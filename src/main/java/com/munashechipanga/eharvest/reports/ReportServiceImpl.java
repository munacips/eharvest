package com.munashechipanga.eharvest.reports;

import com.munashechipanga.eharvest.entities.DeliveryLocation;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.OrderItem;
import com.munashechipanga.eharvest.entities.Produce;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.ReviewStatus;
import com.munashechipanga.eharvest.repositories.DeliveryLocationRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderItemRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.PaymentRepository;
import com.munashechipanga.eharvest.repositories.ProduceRepository;
import com.munashechipanga.eharvest.repositories.ReviewRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.reports.dto.ReportColumn;
import com.munashechipanga.eharvest.reports.dto.ReportDescriptor;
import com.munashechipanga.eharvest.reports.dto.ReportRequestParams;
import com.munashechipanga.eharvest.reports.exceptions.ReportGenerationException;
import com.munashechipanga.eharvest.reports.exceptions.ReportNotAllowedException;
import com.munashechipanga.eharvest.reports.exceptions.ReportNotFoundException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;
    private static final long DEFAULT_SLA_HOURS = 72L;
    private static final String FALLBACK_REPORT_CSS = """
            body { font-family: Arial, sans-serif; color: #1f2933; font-size: 11px; margin: 0; }
            .page { padding: 24px; }
            .header { border-bottom: 2px solid #0f766e; padding-bottom: 12px; margin-bottom: 18px; }
            .company { font-size: 22px; font-weight: bold; color: #0f766e; }
            .title { font-size: 18px; margin-top: 6px; font-weight: bold; }
            .meta { color: #52606d; margin-top: 4px; }
            .description { margin: 14px 0 10px; color: #334e68; }
            .kpis { width: 100%; margin-bottom: 18px; }
            .kpi { display: inline-block; width: 23%; margin-right: 1%; background: #f0fdfa; border: 1px solid #b2f5ea; border-radius: 8px; padding: 10px; vertical-align: top; }
            .kpi-key { color: #486581; text-transform: capitalize; margin-bottom: 6px; }
            .kpi-value { font-size: 16px; font-weight: bold; color: #102a43; }
            table { width: 100%; border-collapse: collapse; }
            th { background: #0f766e; color: #ffffff; text-align: left; padding: 8px; font-size: 10px; }
            td { border-bottom: 1px solid #d9e2ec; padding: 8px; vertical-align: top; }
            .notes { margin-top: 18px; padding: 12px; background: #f8fafc; border: 1px solid #d9e2ec; }
            .notes ul { margin: 8px 0 0 18px; }
            """;

    private final ReportRegistry reportRegistry;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final LogisticsRepository logisticsRepository;
    private final DeliveryLocationRepository deliveryLocationRepository;
    private final ProduceRepository produceRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final String companyName;
    private final long timeoutSeconds;

    public ReportServiceImpl(ReportRegistry reportRegistry,
                             TemplateEngine templateEngine,
                             UserRepository userRepository,
                             OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository,
                             LogisticsRepository logisticsRepository,
                             DeliveryLocationRepository deliveryLocationRepository,
                             ProduceRepository produceRepository,
                             PaymentRepository paymentRepository,
                             ReviewRepository reviewRepository,
                             @Value("${eharvest.reports.company-name:eHarvest}") String companyName,
                             @Value("${eharvest.reports.generation-timeout-seconds:30}") long timeoutSeconds) {
        this.reportRegistry = reportRegistry;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.logisticsRepository = logisticsRepository;
        this.deliveryLocationRepository = deliveryLocationRepository;
        this.produceRepository = produceRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.companyName = companyName;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public List<ReportDescriptor> availableReportsForUser(Authentication auth) {
        Set<String> roles = resolveRoles(auth);
        return reportRegistry.all().stream()
                .filter(descriptor -> descriptor.allowedRoles().stream().anyMatch(roles::contains))
                .toList();
    }

    @Override
    public byte[] generatePdf(String reportName, ReportRequestParams params, Authentication auth) {
        validateParams(params);
        ReportDescriptor descriptor = reportRegistry.find(reportName)
                .orElseThrow(() -> new ReportNotFoundException("Unknown report: " + reportName));
        UserContext userContext = resolveUserContext(auth);
        ensureAllowed(descriptor, userContext.roles());

        try {
            return CompletableFuture.supplyAsync(() -> doGenerate(reportName, descriptor, params, userContext))
                    .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ReportGenerationException("Failed to generate report " + reportName, cause);
        }
    }

    private byte[] doGenerate(String reportName,
                              ReportDescriptor descriptor,
                              ReportRequestParams params,
                              UserContext userContext) {
        GeneratedReportData reportData = switch (reportName) {
            case "sales_summary" -> buildSalesSummary(params, userContext);
            case "orders_fulfillment_funnel" -> buildOrdersFulfillmentFunnel(params, userContext);
            case "deliveries_performance" -> buildDeliveriesPerformance(params, userContext);
            case "deliveries_monthly_list" -> buildDeliveriesMonthlyList(params, userContext);
            case "returns_and_inbound" -> buildReturnsAndInbound(params, userContext);
            case "sku_performance" -> buildSkuPerformance(params, userContext);
            case "inventory_health" -> buildInventoryHealth(params, userContext);
            case "supplier_performance" -> buildSupplierPerformance(params, userContext);
            case "revenue_by_region" -> buildRevenueByRegion(params, userContext);
            case "customer_cohort_retention" -> buildCustomerCohortRetention(params, userContext);
            case "payments_and_refunds" -> buildPaymentsAndRefunds(params, userContext);
            case "reviews_and_complaints" -> buildReviewsAndComplaints(params, userContext);
            default -> throw new ReportNotFoundException("Unknown report: " + reportName);
        };

        Context context = new Context(Locale.ENGLISH);
        context.setVariable("companyName", companyName);
        context.setVariable("reportName", descriptor.reportName());
        context.setVariable("reportTitle", descriptor.label());
        context.setVariable("reportDescription", descriptor.description());
        context.setVariable("generatedBy", userContext.username());
        context.setVariable("generatedAt", LocalDateTime.now());
        context.setVariable("dateRange", formatDateRange(params));
        context.setVariable("summary", reportData.summary());
        context.setVariable("columns", reportData.columns());
        context.setVariable("rows", reportData.rows());
        context.setVariable("notes", reportData.notes());
        context.setVariable("reportCss", loadReportCss());

        String html = templateEngine.process("reports/" + reportName, context);
        return renderPdf(html, reportName);
    }

    private GeneratedReportData buildSalesSummary(ReportRequestParams params, UserContext userContext) {
        List<Order> orders = getScopedOrders(params, userContext);
        Map<LocalDate, List<Order>> byDay = orders.stream()
                .filter(order -> order.getOrderDate() != null)
                .collect(Collectors.groupingBy(order -> order.getOrderDate().toLocalDate(), TreeMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = byDay.entrySet().stream()
                .map(entry -> {
                    List<Order> dayOrders = entry.getValue();
                    double dayRevenue = dayOrders.stream().mapToDouble(order -> safeDouble(order.getTotalAmount())).sum();
                    return row(
                            "day", entry.getKey(),
                            "orderCount", dayOrders.size(),
                            "revenue", round(dayRevenue),
                            "avgOrderValue", round(dayRevenue / Math.max(dayOrders.size(), 1))
                    );
                })
                .toList();

        double totalRevenue = orders.stream().mapToDouble(order -> safeDouble(order.getTotalAmount())).sum();
        return new GeneratedReportData(
                summary(
                        "totalRevenue", round(totalRevenue),
                        "orderCount", orders.size(),
                        "avgOrderValue", round(totalRevenue / Math.max(orders.size(), 1)),
                        "daysCovered", rows.size()
                ),
                List.of(
                        new ReportColumn("day", "Day"),
                        new ReportColumn("orderCount", "Orders"),
                        new ReportColumn("revenue", "Revenue"),
                        new ReportColumn("avgOrderValue", "Avg Order Value")
                ),
                rows,
                List.of("Revenue is derived from orders.totalAmount within the selected date range.")
        );
    }

    private GeneratedReportData buildOrdersFulfillmentFunnel(ReportRequestParams params, UserContext userContext) {
        List<Order> orders = getScopedOrders(params, userContext);
        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(order -> normalizeStatus(order.getStatus()), LinkedHashMap::new, Collectors.counting()));

        List<Map<String, Object>> rows = List.of(
                funnelRow("placed", (long) orders.size()),
                funnelRow("picked", statusCounts.getOrDefault("ACCEPTED", 0L)),
                funnelRow("packed", statusCounts.getOrDefault("AWAITING_TRANSPORT_FEE_APPROVAL", 0L)),
                funnelRow("dispatched", statusCounts.getOrDefault("IN_TRANSIT", 0L)),
                funnelRow("delivered", statusCounts.getOrDefault("DELIVERED", 0L)),
                funnelRow("cancelled", statusCounts.getOrDefault("CANCELLED", 0L) + statusCounts.getOrDefault("REJECTED", 0L))
        );

        return new GeneratedReportData(
                summary(
                        "placed", orders.size(),
                        "delivered", statusCounts.getOrDefault("DELIVERED", 0L),
                        "cancelled", statusCounts.getOrDefault("CANCELLED", 0L) + statusCounts.getOrDefault("REJECTED", 0L),
                        "deliveryRate", percentage(statusCounts.getOrDefault("DELIVERED", 0L), orders.size())
                ),
                List.of(
                        new ReportColumn("stage", "Stage"),
                        new ReportColumn("count", "Count"),
                        new ReportColumn("avgStageHours", "Avg Stage Hours")
                ),
                rows,
                List.of("Picked and packed counts are approximated from ACCEPTED and AWAITING_TRANSPORT_FEE_APPROVAL because the current schema does not persist explicit warehouse stage timestamps.")
        );
    }

    private GeneratedReportData buildDeliveriesPerformance(ReportRequestParams params, UserContext userContext) {
        List<LogisticsRequest> requests = getScopedLogisticsRequests(params, userContext);
        List<Map<String, Object>> rows = requests.stream()
                .map(request -> {
                    Duration duration = deliveryDuration(request);
                    boolean onTime = duration != null && duration.toHours() <= DEFAULT_SLA_HOURS;
                    return row(
                            "orderId", request.getOrder() != null ? request.getOrder().getId() : null,
                            "provider", request.getAssignedProvider() != null ? fullName(request.getAssignedProvider()) : "Unassigned",
                            "status", normalizeStatus(request.getStatus()),
                            "attemptedAt", request.getOrder() != null ? request.getOrder().getOrderDate() : null,
                            "completedAt", latestDeliveryUpdate(request.getOrder()),
                            "onTime", onTime ? "Yes" : "No"
                    );
                })
                .toList();

        long succeeded = requests.stream().filter(request -> "DELIVERED".equals(normalizeStatus(request.getStatus()))).count();
        long failed = requests.stream().filter(request -> "REJECTED".equals(normalizeStatus(request.getStatus()))).count();
        long onTimeCount = requests.stream()
                .filter(request -> {
                    Duration duration = deliveryDuration(request);
                    return duration != null && duration.toHours() <= DEFAULT_SLA_HOURS;
                })
                .count();

        return new GeneratedReportData(
                summary(
                        "attempted", requests.size(),
                        "succeeded", succeeded,
                        "failed", failed,
                        "onTimePercent", percentage(onTimeCount, Math.max(succeeded, 1))
                ),
                List.of(
                        new ReportColumn("orderId", "Order"),
                        new ReportColumn("provider", "Driver"),
                        new ReportColumn("status", "Status"),
                        new ReportColumn("attemptedAt", "Attempted At"),
                        new ReportColumn("completedAt", "Completed At"),
                        new ReportColumn("onTime", "On Time")
                ),
                rows,
                List.of("On-time delivery uses a 72-hour SLA approximation between orderDate and the latest delivery tracker update.")
        );
    }

    private GeneratedReportData buildDeliveriesMonthlyList(ReportRequestParams params, UserContext userContext) {
        List<LogisticsRequest> requests = getScopedLogisticsRequests(params, userContext);
        List<Map<String, Object>> rows = requests.stream()
                .map(request -> row(
                        "orderId", request.getOrder() != null ? request.getOrder().getId() : null,
                        "customer", request.getOrder() != null && request.getOrder().getBuyer() != null ? fullName(request.getOrder().getBuyer()) : "Unknown",
                        "address", request.getDeliveryLocation(),
                        "driver", request.getAssignedProvider() != null ? fullName(request.getAssignedProvider()) : "Unassigned",
                        "status", normalizeStatus(request.getStatus()),
                        "timestamp", Optional.ofNullable(latestDeliveryUpdate(request.getOrder()))
                                .orElse(request.getOrder() != null ? request.getOrder().getOrderDate() : null),
                        "podLink", "Not available in current schema"
                ))
                .toList();

        return new GeneratedReportData(
                summary(
                        "deliveryRows", rows.size(),
                        "delivered", requests.stream().filter(request -> "DELIVERED".equals(normalizeStatus(request.getStatus()))).count(),
                        "inTransit", requests.stream().filter(request -> "IN_TRANSIT".equals(normalizeStatus(request.getStatus()))).count(),
                        "unassigned", requests.stream().filter(request -> request.getAssignedProvider() == null).count()
                ),
                List.of(
                        new ReportColumn("orderId", "Order"),
                        new ReportColumn("customer", "Customer"),
                        new ReportColumn("address", "Address"),
                        new ReportColumn("driver", "Driver"),
                        new ReportColumn("status", "Status"),
                        new ReportColumn("timestamp", "Timestamp"),
                        new ReportColumn("podLink", "POD Link")
                ),
                rows,
                List.of("Proof-of-delivery links are not yet stored in the current domain model.")
        );
    }

    private GeneratedReportData buildReturnsAndInbound(ReportRequestParams params, UserContext userContext) {
        List<Order> orders = getScopedOrders(params, userContext);
        Map<String, Long> outcomes = orders.stream()
                .map(order -> normalizeStatus(order.getStatus()))
                .filter(status -> "REJECTED".equals(status) || "CANCELLED".equals(status))
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        long total = outcomes.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> rows = outcomes.entrySet().stream()
                .map(entry -> row(
                        "reasonCode", entry.getKey(),
                        "count", entry.getValue(),
                        "restockablePercent", "CANCELLED".equals(entry.getKey()) ? 100.0 : 0.0
                ))
                .toList();

        return new GeneratedReportData(
                summary(
                        "returnsReceived", total,
                        "cancelled", outcomes.getOrDefault("CANCELLED", 0L),
                        "rejected", outcomes.getOrDefault("REJECTED", 0L),
                        "restockablePercent", total == 0 ? 0.0 : percentage(outcomes.getOrDefault("CANCELLED", 0L), total)
                ),
                List.of(
                        new ReportColumn("reasonCode", "Reason"),
                        new ReportColumn("count", "Count"),
                        new ReportColumn("restockablePercent", "Restockable %")
                ),
                rows,
                List.of("This project has no dedicated returns entity yet, so this report derives return-like events from CANCELLED and REJECTED orders.")
        );
    }

    private GeneratedReportData buildSkuPerformance(ReportRequestParams params, UserContext userContext) {
        List<OrderItem> items = getScopedOrderItems(params, userContext);
        Map<Long, List<OrderItem>> byProduce = items.stream()
                .filter(item -> item.getProduce() != null)
                .collect(Collectors.groupingBy(item -> item.getProduce().getId(), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = byProduce.values().stream()
                .map(group -> {
                    Produce produce = group.getFirst().getProduce();
                    int units = group.stream().mapToInt(item -> Optional.ofNullable(item.getQuantity()).orElse(0)).sum();
                    double revenue = group.stream().mapToDouble(item -> safeDouble(item.getPrice()) * Optional.ofNullable(item.getQuantity()).orElse(0)).sum();
                    return row(
                            "sku", produce.getName(),
                            "category", produce.getCategory(),
                            "units", units,
                            "revenue", round(revenue),
                            "margin", round(revenue * 0.15),
                            "inventoryOnHand", safeDouble(produce.getQuantity()),
                            "movement", units >= 10 ? "Top seller" : "Low mover"
                    );
                })
                .sorted(Comparator.comparing((Map<String, Object> map) -> safeDouble(map.get("revenue"))).reversed())
                .toList();

        return new GeneratedReportData(
                summary(
                        "trackedSkus", rows.size(),
                        "topSellers", rows.stream().filter(row -> "Top seller".equals(row.get("movement"))).count(),
                        "lowMovers", rows.stream().filter(row -> "Low mover".equals(row.get("movement"))).count(),
                        "revenue", round(rows.stream().mapToDouble(row -> safeDouble(row.get("revenue"))).sum())
                ),
                List.of(
                        new ReportColumn("sku", "SKU"),
                        new ReportColumn("category", "Category"),
                        new ReportColumn("units", "Units"),
                        new ReportColumn("revenue", "Revenue"),
                        new ReportColumn("margin", "Margin"),
                        new ReportColumn("inventoryOnHand", "On Hand"),
                        new ReportColumn("movement", "Movement")
                ),
                rows,
                List.of("Margin is estimated at 15% of revenue because the current entities do not persist cost of goods sold.")
        );
    }

    private GeneratedReportData buildInventoryHealth(ReportRequestParams params, UserContext userContext) {
        List<Produce> produceList = getScopedProduce(userContext);
        List<OrderItem> soldItems = getScopedOrderItems(params, userContext);
        long rangeDays = Math.max(1L, daysBetweenInclusive(params));

        Map<Long, Integer> soldByProduce = soldItems.stream()
                .filter(item -> item.getProduce() != null)
                .collect(Collectors.groupingBy(item -> item.getProduce().getId(),
                        Collectors.summingInt(item -> Optional.ofNullable(item.getQuantity()).orElse(0))));

        List<Map<String, Object>> rows = produceList.stream()
                .map(produce -> {
                    double onHand = safeDouble(produce.getQuantity());
                    int soldUnits = soldByProduce.getOrDefault(produce.getId(), 0);
                    double dailyBurn = soldUnits / (double) rangeDays;
                    double daysOfCover = dailyBurn == 0 ? onHand : onHand / dailyBurn;
                    return row(
                            "sku", produce.getName(),
                            "region", produce.getCityTown(),
                            "onHand", round(onHand),
                            "reserved", 0,
                            "safetyStock", 10,
                            "daysOfCover", round(daysOfCover),
                            "alert", onHand <= 0 ? "STOCKOUT" : (onHand < 10 ? "LOW" : "HEALTHY")
                    );
                })
                .sorted(Comparator.comparing(row -> String.valueOf(row.get("alert"))))
                .toList();

        return new GeneratedReportData(
                summary(
                        "skus", rows.size(),
                        "inStock", rows.stream().filter(row -> !"STOCKOUT".equals(row.get("alert"))).count(),
                        "stockoutAlerts", rows.stream().filter(row -> "STOCKOUT".equals(row.get("alert"))).count(),
                        "lowStockAlerts", rows.stream().filter(row -> "LOW".equals(row.get("alert"))).count()
                ),
                List.of(
                        new ReportColumn("sku", "SKU"),
                        new ReportColumn("region", "Region"),
                        new ReportColumn("onHand", "On Hand"),
                        new ReportColumn("reserved", "Reserved"),
                        new ReportColumn("safetyStock", "Safety Stock"),
                        new ReportColumn("daysOfCover", "Days Of Cover"),
                        new ReportColumn("alert", "Alert")
                ),
                rows,
                List.of("Reserved and safety stock are placeholders because dedicated inventory reservation entities are not present yet.")
        );
    }

    private GeneratedReportData buildSupplierPerformance(ReportRequestParams params, UserContext userContext) {
        List<Order> orders = getScopedSupplierOrders(params, userContext);
        Map<Long, List<Order>> byFarmer = orders.stream()
                .filter(order -> order.getFarmer() != null)
                .collect(Collectors.groupingBy(order -> order.getFarmer().getId(), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = byFarmer.values().stream()
                .map(group -> {
                    User farmer = group.getFirst().getFarmer();
                    long supplied = group.size();
                    long delivered = group.stream().filter(order -> "DELIVERED".equals(normalizeStatus(order.getStatus()))).count();
                    long rejected = group.stream().filter(order -> {
                        String status = normalizeStatus(order.getStatus());
                        return "REJECTED".equals(status) || "CANCELLED".equals(status);
                    }).count();
                    long onTime = group.stream()
                            .filter(order -> {
                                LogisticsRequest logistics = order.getLogisticsRequest();
                                if (logistics == null) {
                                    return false;
                                }
                                Duration duration = deliveryDuration(logistics);
                                return duration != null && duration.toHours() <= DEFAULT_SLA_HOURS;
                            })
                            .count();
                    return row(
                            "supplier", fullName(farmer),
                            "deliveriesSupplied", supplied,
                            "delivered", delivered,
                            "onTimePercent", percentage(onTime, Math.max(delivered, 1)),
                            "rejects", rejected
                    );
                })
                .toList();

        return new GeneratedReportData(
                summary(
                        "suppliers", rows.size(),
                        "deliveriesSupplied", rows.stream().mapToLong(row -> ((Number) row.get("deliveriesSupplied")).longValue()).sum(),
                        "delivered", rows.stream().mapToLong(row -> ((Number) row.get("delivered")).longValue()).sum(),
                        "rejects", rows.stream().mapToLong(row -> ((Number) row.get("rejects")).longValue()).sum()
                ),
                List.of(
                        new ReportColumn("supplier", "Supplier"),
                        new ReportColumn("deliveriesSupplied", "Supplied"),
                        new ReportColumn("delivered", "Delivered"),
                        new ReportColumn("onTimePercent", "On-Time %"),
                        new ReportColumn("rejects", "Rejects")
                ),
                rows,
                List.of("Supplier timing is approximated from order creation to the latest tracked delivery update.")
        );
    }

    private GeneratedReportData buildRevenueByRegion(ReportRequestParams params, UserContext userContext) {
        List<OrderItem> items = getScopedOrderItems(params, userContext);
        Map<String, List<OrderItem>> byRegion = items.stream()
                .collect(Collectors.groupingBy(item -> Optional.ofNullable(item.getProduce()).map(Produce::getCityTown).orElse("Unknown"),
                        LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = byRegion.entrySet().stream()
                .map(entry -> {
                    double revenue = entry.getValue().stream()
                            .mapToDouble(item -> safeDouble(item.getPrice()) * Optional.ofNullable(item.getQuantity()).orElse(0))
                            .sum();
                    long orders = entry.getValue().stream()
                            .map(OrderItem::getOrder)
                            .filter(Objects::nonNull)
                            .map(Order::getId)
                            .distinct()
                            .count();
                    return row(
                            "region", entry.getKey(),
                            "orders", orders,
                            "revenue", round(revenue)
                    );
                })
                .sorted(Comparator.comparing((Map<String, Object> map) -> safeDouble(map.get("revenue"))).reversed())
                .toList();

        return new GeneratedReportData(
                summary(
                        "regions", rows.size(),
                        "orders", rows.stream().mapToLong(row -> ((Number) row.get("orders")).longValue()).sum(),
                        "revenue", round(rows.stream().mapToDouble(row -> safeDouble(row.get("revenue"))).sum()),
                        "topRegion", rows.isEmpty() ? "N/A" : rows.getFirst().get("region")
                ),
                List.of(
                        new ReportColumn("region", "Region"),
                        new ReportColumn("orders", "Orders"),
                        new ReportColumn("revenue", "Revenue")
                ),
                rows,
                List.of("Region is derived from produce.cityTown because the order aggregate does not currently store a normalized geographic dimension.")
        );
    }

    private GeneratedReportData buildCustomerCohortRetention(ReportRequestParams params, UserContext userContext) {
        List<Order> orders = getScopedOrders(params, userContext);
        Map<Long, List<Order>> byBuyer = orders.stream()
                .filter(order -> order.getBuyer() != null && order.getOrderDate() != null)
                .collect(Collectors.groupingBy(order -> order.getBuyer().getId()));

        Map<YearMonth, List<List<Order>>> cohorts = byBuyer.values().stream()
                .collect(Collectors.groupingBy(orderList -> YearMonth.from(orderList.stream()
                                .map(Order::getOrderDate)
                                .min(LocalDateTime::compareTo)
                                .orElseThrow()),
                        TreeMap::new,
                        Collectors.toList()));

        List<Map<String, Object>> rows = cohorts.entrySet().stream()
                .map(entry -> {
                    int cohortSize = entry.getValue().size();
                    long repeatCustomers = entry.getValue().stream()
                            .filter(orderList -> orderList.stream()
                                    .map(order -> YearMonth.from(order.getOrderDate()))
                                    .distinct()
                                    .count() > 1)
                            .count();
                    return row(
                            "cohortMonth", entry.getKey(),
                            "cohortSize", cohortSize,
                            "repeatPurchaseCustomers", repeatCustomers,
                            "retentionRate", percentage(repeatCustomers, Math.max(cohortSize, 1))
                    );
                })
                .toList();

        long cohortCustomers = rows.stream().mapToLong(row -> ((Number) row.get("cohortSize")).longValue()).sum();
        long repeatCustomers = rows.stream().mapToLong(row -> ((Number) row.get("repeatPurchaseCustomers")).longValue()).sum();

        return new GeneratedReportData(
                summary(
                        "cohorts", rows.size(),
                        "customers", cohortCustomers,
                        "repeatCustomers", repeatCustomers,
                        "repeatPurchasePercent", percentage(repeatCustomers, Math.max(cohortCustomers, 1))
                ),
                List.of(
                        new ReportColumn("cohortMonth", "Cohort Month"),
                        new ReportColumn("cohortSize", "Cohort Size"),
                        new ReportColumn("repeatPurchaseCustomers", "Repeat Customers"),
                        new ReportColumn("retentionRate", "Retention %")
                ),
                rows,
                List.of("Buyer-scoped retention is naturally narrow in the current schema because buyers are themselves the purchasing accounts.")
        );
    }

    private GeneratedReportData buildPaymentsAndRefunds(ReportRequestParams params, UserContext userContext) {
        List<TransactionHistory> transactions = getScopedTransactions(params, userContext);
        Map<String, List<TransactionHistory>> byStatus = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> normalizeStatus(transaction.getStatus()), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> rows = byStatus.entrySet().stream()
                .map(entry -> row(
                        "status", entry.getKey(),
                        "count", entry.getValue().size(),
                        "amount", round(entry.getValue().stream().mapToDouble(tx -> safeDouble(tx.getAmount())).sum()),
                        "providers", entry.getValue().stream().map(TransactionHistory::getProvider).filter(Objects::nonNull).distinct().sorted().collect(Collectors.joining(", "))
                ))
                .toList();

        double totalAmount = transactions.stream().mapToDouble(tx -> safeDouble(tx.getAmount())).sum();
        long refunds = transactions.stream()
                .filter(tx -> tx.getType() != null && tx.getType().name().contains("REFUND"))
                .count();

        return new GeneratedReportData(
                summary(
                        "transactions", transactions.size(),
                        "totalAmount", round(totalAmount),
                        "successfulPayments", transactions.stream().filter(tx -> "SUCCESS".equals(normalizeStatus(tx.getStatus())) || "PAID".equals(normalizeStatus(tx.getStatus()))).count(),
                        "refunds", refunds
                ),
                List.of(
                        new ReportColumn("status", "Status"),
                        new ReportColumn("count", "Count"),
                        new ReportColumn("amount", "Amount"),
                        new ReportColumn("providers", "Providers")
                ),
                rows,
                List.of("Refund counts depend on transaction type values containing REFUND in the current payment ledger.")
        );
    }

    private GeneratedReportData buildReviewsAndComplaints(ReportRequestParams params, UserContext userContext) {
        List<Review> reviews = reviewRepository.findAllForReportBetween(rangeStart(params), rangeEnd(params));
        if (userContext.roles().contains("ROLE_FARMER")) {
            reviews = reviews.stream()
                    .filter(review -> review.getReviewee() != null && Objects.equals(review.getReviewee().getId(), userContext.userId()))
                    .toList();
        }

        List<Map<String, Object>> rows = reviews.stream()
                .map(review -> row(
                        "createdAt", review.getCreatedAt(),
                        "reviewer", review.getReviewer() != null ? fullName(review.getReviewer()) : "Unknown",
                        "reviewee", review.getReviewee() != null ? fullName(review.getReviewee()) : "Unknown",
                        "rating", review.getRating(),
                        "status", Optional.ofNullable(review.getStatus()).orElse(ReviewStatus.PENDING).name(),
                        "comment", Optional.ofNullable(review.getComment()).orElse("")
                ))
                .toList();

        double averageRating = reviews.stream()
                .filter(review -> review.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return new GeneratedReportData(
                summary(
                        "reviews", reviews.size(),
                        "averageRating", round(averageRating),
                        "complaints", reviews.stream().filter(review -> Optional.ofNullable(review.getRating()).orElse(0) <= 2).count(),
                        "completed", reviews.stream().filter(review -> review.getStatus() == ReviewStatus.COMPLETED).count()
                ),
                List.of(
                        new ReportColumn("createdAt", "Created At"),
                        new ReportColumn("reviewer", "Reviewer"),
                        new ReportColumn("reviewee", "Reviewee"),
                        new ReportColumn("rating", "Rating"),
                        new ReportColumn("status", "Status"),
                        new ReportColumn("comment", "Comment")
                ),
                rows,
                List.of("Complaint volume is approximated as ratings of 2 stars or below.")
        );
    }

    private List<Order> getScopedOrders(ReportRequestParams params, UserContext userContext) {
        if (userContext.roles().contains("ROLE_BUYER")) {
            return orderRepository.findAllForBuyerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        if (userContext.roles().contains("ROLE_FARMER")) {
            return orderRepository.findAllForFarmerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        return orderRepository.findAllForReportBetween(rangeStart(params), rangeEnd(params));
    }

    private List<Order> getScopedSupplierOrders(ReportRequestParams params, UserContext userContext) {
        if (userContext.roles().contains("ROLE_FARMER")) {
            return orderRepository.findAllForFarmerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        if (params.getFarmerId() != null) {
            return orderRepository.findAllForFarmerReportBetween(params.getFarmerId(), rangeStart(params), rangeEnd(params));
        }
        return orderRepository.findAllForReportBetween(rangeStart(params), rangeEnd(params));
    }

    private List<OrderItem> getScopedOrderItems(ReportRequestParams params, UserContext userContext) {
        if (userContext.roles().contains("ROLE_FARMER")) {
            return orderItemRepository.findAllForFarmerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        if (userContext.roles().contains("ROLE_BUYER")) {
            return orderItemRepository.findAllForBuyerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        return orderItemRepository.findAllForReportBetween(rangeStart(params), rangeEnd(params));
    }

    private List<LogisticsRequest> getScopedLogisticsRequests(ReportRequestParams params, UserContext userContext) {
        if (userContext.roles().contains("ROLE_LOGISTICS")) {
            return logisticsRepository.findAllForProviderReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        if (userContext.roles().contains("ROLE_BUYER")) {
            return logisticsRepository.findAllForBuyerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        return logisticsRepository.findAllForReportBetween(rangeStart(params), rangeEnd(params));
    }

    private List<Produce> getScopedProduce(UserContext userContext) {
        if (userContext.roles().contains("ROLE_FARMER")) {
            return produceRepository.findByFarmer_Id(userContext.userId());
        }
        return produceRepository.findAll();
    }

    private List<TransactionHistory> getScopedTransactions(ReportRequestParams params, UserContext userContext) {
        if (userContext.roles().contains("ROLE_BUYER")) {
            return paymentRepository.findAllForBuyerReportBetween(userContext.userId(), rangeStart(params), rangeEnd(params));
        }
        return paymentRepository.findAllForReportBetween(rangeStart(params), rangeEnd(params));
    }

    private void validateParams(ReportRequestParams params) {
        if (params.getFrom() != null && params.getTo() != null && params.getFrom().isAfter(params.getTo())) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equal to 'to'.");
        }
    }

    private void ensureAllowed(ReportDescriptor descriptor, Set<String> roles) {
        boolean allowed = descriptor.allowedRoles().stream().anyMatch(roles::contains);
        if (!allowed) {
            throw new ReportNotAllowedException("You are not allowed to generate report '" + descriptor.reportName() + "'.");
        }
    }

    private UserContext resolveUserContext(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ReportNotAllowedException("Authentication is required.");
        }

        Set<String> roles = resolveRoles(auth);
        String username = extractUsername(auth);
        if ("AI_SERVICE".equals(username)) {
            return new UserContext(null, username, roles);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ReportNotAllowedException("Authenticated user could not be resolved."));
        return new UserContext(user.getId(), user.getUsername(), roles);
    }

    private Set<String> resolveRoles(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private String extractUsername(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return String.valueOf(principal);
    }

    private byte[] renderPdf(String html, String reportName) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            log.error("Failed to render PDF for report {}", reportName, ex);
            throw new ReportGenerationException("Failed to render PDF for " + reportName, ex);
        }
    }

    private String loadReportCss() {
        try {
            return new ClassPathResource("templates/reports/report.css")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.warn("Report stylesheet not found on classpath; using embedded fallback CSS.", ex);
            return FALLBACK_REPORT_CSS;
        }
    }

    private LocalDateTime rangeStart(ReportRequestParams params) {
        return Optional.ofNullable(params.getFrom()).orElse(LocalDate.now().minusMonths(1)).atStartOfDay();
    }

    private LocalDateTime rangeEnd(ReportRequestParams params) {
        return Optional.ofNullable(params.getTo()).orElse(LocalDate.now()).atTime(23, 59, 59);
    }

    private long daysBetweenInclusive(ReportRequestParams params) {
        LocalDate from = Optional.ofNullable(params.getFrom()).orElse(LocalDate.now().minusMonths(1));
        LocalDate to = Optional.ofNullable(params.getTo()).orElse(LocalDate.now());
        return Duration.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay()).toDays();
    }

    private String formatDateRange(ReportRequestParams params) {
        String from = params.getFrom() != null ? params.getFrom().format(DATE_FORMAT) : "start";
        String to = params.getTo() != null ? params.getTo().format(DATE_FORMAT) : "today";
        return from + " to " + to;
    }

    private Map<String, Object> funnelRow(String stage, long count) {
        return row("stage", stage, "count", count, "avgStageHours", "N/A");
    }

    private Duration deliveryDuration(LogisticsRequest request) {
        if (request == null || request.getOrder() == null || request.getOrder().getOrderDate() == null) {
            return null;
        }
        LocalDateTime completedAt = latestDeliveryUpdate(request.getOrder());
        if (completedAt == null) {
            return null;
        }
        return Duration.between(request.getOrder().getOrderDate(), completedAt);
    }

    private LocalDateTime latestDeliveryUpdate(Order order) {
        if (order == null || order.getId() == null) {
            return null;
        }
        return deliveryLocationRepository.findByOrder_Id(order.getId())
                .map(DeliveryLocation::getUpdatedAt)
                .orElse(null);
    }

    private String fullName(User user) {
        if (user == null) {
            return "Unknown";
        }
        return List.of(user.getFirstName(), user.getLastName()).stream()
                .filter(Objects::nonNull)
                .filter(part -> !part.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }

    private String normalizeStatus(String status) {
        return status == null ? "UNKNOWN" : status.trim().toUpperCase(Locale.ROOT);
    }

    private Map<String, Object> summary(Object... keyValues) {
        Map<String, Object> summary = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            summary.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return summary;
    }

    private Map<String, Object> row(Object... keyValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            row.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return row;
    }

    private double safeDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private double percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return round((numerator * 100.0) / denominator);
    }

    private record GeneratedReportData(Map<String, Object> summary,
                                       List<ReportColumn> columns,
                                       List<Map<String, Object>> rows,
                                       List<String> notes) {
    }

    private record UserContext(Long userId, String username, Set<String> roles) {
    }
}
