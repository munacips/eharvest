package com.munashechipanga.eharvest.reports;

import com.munashechipanga.eharvest.reports.dto.ReportDescriptor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ReportRegistry {

    private final Map<String, ReportDescriptor> descriptors;

    public ReportRegistry() {
        Map<String, ReportDescriptor> registry = new LinkedHashMap<>();
        register(registry, "sales_summary", "Sales Summary",
                "Revenue, order count, and average order value by day.",
                List.of("ADMIN", "BUYER"),
                List.of("from", "to"));
        register(registry, "orders_fulfillment_funnel", "Orders Fulfillment Funnel",
                "Volume across order lifecycle stages with timing approximations from existing delivery data.",
                List.of("ROLE_ADMIN", "ROLE_LOGISTICS"),
                List.of("from", "to"));
        register(registry, "deliveries_performance", "Deliveries Performance",
                "Attempted, successful, failed deliveries and on-time percentage.",
                List.of("ADMIN", "LOGISTICS"),
                List.of("from", "to"));
        register(registry, "deliveries_monthly_list", "Deliveries Monthly List",
                "Detailed delivery rows for a selected period.",
                List.of("ADMIN", "LOGISTICS", "BUYER"),
                List.of("from", "to"));
        register(registry, "returns_and_inbound", "Returns And Inbound",
                "Return-like outcomes and inbound recovery indicators derived from current order lifecycle statuses.",
                List.of("ADMIN", "BUYER"),
                List.of("from", "to"));
        register(registry, "sku_performance", "SKU Performance",
                "Top-selling and low-moving SKUs with revenue and current stock levels.",
                List.of("ADMIN", "BUYER", "FARMER"),
                List.of("from", "to", "farmerId"));
        register(registry, "inventory_health", "Inventory Health",
                "Current stock posture, stockout alerts, and estimated days of cover.",
                List.of("ADMIN", "LOGISTICS", "BUYER"),
                List.of("from", "to"));
        register(registry, "supplier_performance", "Supplier Performance",
                "Farmer supply performance including fulfillment and rejection trends.",
                List.of("ADMIN", "FARMER", "BUYER"),
                List.of("from", "to", "farmerId"));
        register(registry, "revenue_by_region", "Revenue By Region",
                "Orders and revenue grouped by produce city or town.",
                List.of("ADMIN", "BUYER"),
                List.of("from", "to"));
        register(registry, "customer_cohort_retention", "Customer Cohort Retention",
                "Buyer cohorts by first-order month and repeat purchase rate.",
                List.of("ADMIN", "BUYER", "MARKETING"),
                List.of("from", "to"));
        register(registry, "payments_and_refunds", "Payments And Refunds",
                "Transaction totals, payment statuses, and refund-like reversals by provider.",
                List.of("ADMIN", "BUYER"),
                List.of("from", "to"));
        register(registry, "reviews_and_complaints", "Reviews And Complaints",
                "Ratings, moderation status, and complaint-style comments from reviews.",
                List.of("ADMIN", "BUYER", "FARMER"),
                List.of("from", "to"));
        this.descriptors = Map.copyOf(registry);
    }

    public List<ReportDescriptor> all() {
        return descriptors.values().stream().toList();
    }

    public Optional<ReportDescriptor> find(String reportName) {
        return Optional.ofNullable(descriptors.get(reportName));
    }

    private void register(Map<String, ReportDescriptor> registry,
                          String reportName,
                          String label,
                          String description,
                          List<String> allowedRoles,
                          List<String> params) {
        registry.put(reportName, new ReportDescriptor(reportName, label, description, allowedRoles, params));
    }
}
