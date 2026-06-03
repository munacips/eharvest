package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.*;
import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.request.CreateProduceDTO;
import com.munashechipanga.eharvest.dtos.request.LogisticsRequestCreateDTO;
import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import com.munashechipanga.eharvest.dtos.response.AdminDashboardStatsDTO;
import com.munashechipanga.eharvest.dtos.response.DisputeReportResponseDTO;
import com.munashechipanga.eharvest.dtos.response.OrderItemResponseDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;
import com.munashechipanga.eharvest.dtos.response.SubscriptionResponseDTO;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.*;
import com.munashechipanga.eharvest.services.*;
import com.munashechipanga.eharvest.specs.TransactionSpecifications;
import com.munashechipanga.eharvest.utils.PagingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DisputeReportService disputeReportService;

    @Autowired
    private ProduceService produceService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private LogisticsProviderService logisticsProviderService;

    @Autowired
    private LogisticsService logisticsService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private LogisticsProviderRepository logisticsProviderRepository;

    @Autowired
    private ProduceRepository produceRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private DisputeReportRepository disputeReportRepository;

    // --- Dashboard ---

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalFarmers(farmerRepository.count());
        stats.setTotalBuyers(buyerRepository.count());
        stats.setTotalLogisticsProviders(logisticsProviderRepository.count());
        stats.setTotalAdmins(userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .count());
        stats.setTotalProduce(produceRepository.count());
        stats.setTotalVehicles(vehicleRepository.count());
        stats.setTotalOrders(orderRepository.count());
        stats.setTotalReviews(reviewRepository.count());
        stats.setTotalDisputeReports(disputeReportRepository.count());
        stats.setUnattendedDisputeReports(disputeReportRepository.findByAttendedToOrderByCreatedAtDesc(false).size());
        stats.setUnverifiedUsers(userRepository.findAll().stream()
                .filter(u -> Boolean.FALSE.equals(u.getVerified()))
                .count());
        return ResponseEntity.ok(stats);
    }

    // --- Users ---

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long id, @RequestParam boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(active);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/verified")
    public ResponseEntity<UserResponseDTO> setVerified(@PathVariable Long id, @RequestParam boolean verified) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerified(verified);
        userRepository.save(user);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // --- Farmers ---

    @GetMapping("/farmers")
    public ResponseEntity<Page<UserResponseDTO>> searchFarmers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String farmName,
            @RequestParam(required = false) String farmLocation,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Integer minTrustScore,
            @RequestParam(required = false) Integer maxTrustScore,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        Sort sortSpec = PagingUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        FarmerFilter filter = new FarmerFilter();
        filter.setFirstName(firstName);
        filter.setLastName(lastName);
        filter.setUsername(username);
        filter.setEmail(email);
        filter.setFarmName(farmName);
        filter.setFarmLocation(farmLocation);
        filter.setActive(active);
        filter.setVerified(verified);
        filter.setMinTrustScore(minTrustScore);
        filter.setMaxTrustScore(maxTrustScore);
        filter.setSearch(search);
        return ResponseEntity.ok(farmerService.search(filter, pageable));
    }

    @GetMapping("/farmers/{id}")
    public ResponseEntity<UserResponseDTO> getFarmer(@PathVariable Long id) {
        return ResponseEntity.ok(farmerService.getFarmerById(id));
    }

    @PostMapping("/farmers")
    public ResponseEntity<UserResponseDTO> createFarmer(@RequestBody FarmerDto dto) {
        return ResponseEntity.ok(farmerService.createFarmer(dto));
    }

    @PutMapping("/farmers/{id}")
    public ResponseEntity<UserResponseDTO> updateFarmer(@PathVariable Long id, @RequestBody FarmerDto dto) {
        return ResponseEntity.ok(farmerService.updateFarmer(id, dto));
    }

    @DeleteMapping("/farmers/{id}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.ok().build();
    }

    // --- Buyers ---

    @GetMapping("/buyers")
    public ResponseEntity<Page<UserResponseDTO>> searchBuyers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Integer minTrustScore,
            @RequestParam(required = false) Integer maxTrustScore,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        Sort sortSpec = PagingUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        BuyerFilter filter = new BuyerFilter();
        filter.setFirstName(firstName);
        filter.setLastName(lastName);
        filter.setUsername(username);
        filter.setEmail(email);
        filter.setCompanyName(companyName);
        filter.setActive(active);
        filter.setVerified(verified);
        filter.setMinTrustScore(minTrustScore);
        filter.setMaxTrustScore(maxTrustScore);
        filter.setSearch(search);
        return ResponseEntity.ok(buyerService.search(filter, pageable));
    }

    @GetMapping("/buyers/{id}")
    public ResponseEntity<UserResponseDTO> getBuyer(@PathVariable Long id) {
        return ResponseEntity.ok(buyerService.getBuyerById(id));
    }

    @PostMapping("/buyers")
    public ResponseEntity<UserResponseDTO> createBuyer(@RequestBody BuyerDto dto) {
        return ResponseEntity.ok(buyerService.createBuyer(dto));
    }

    @PutMapping("/buyers/{id}")
    public ResponseEntity<UserResponseDTO> updateBuyer(@PathVariable Long id, @RequestBody BuyerDto dto) {
        return ResponseEntity.ok(buyerService.updateBuyer(id, dto));
    }

    @DeleteMapping("/buyers/{id}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable Long id) {
        buyerService.deleteBuyer(id);
        return ResponseEntity.ok().build();
    }

    // --- Logistics providers ---

    @GetMapping("/logistics-providers")
    public ResponseEntity<List<UserResponseDTO>> getAllLogisticsProviders() {
        return ResponseEntity.ok(logisticsProviderService.getAllLogisticsProviders());
    }

    @GetMapping("/logistics-providers/{id}")
    public ResponseEntity<UserResponseDTO> getLogisticsProvider(@PathVariable Long id) {
        return ResponseEntity.ok(logisticsProviderService.getLogisticsProviderById(id));
    }

    @PostMapping("/logistics-providers")
    public ResponseEntity<UserResponseDTO> createLogisticsProvider(@RequestBody LogisticsProviderDto dto) {
        return ResponseEntity.ok(logisticsProviderService.createLogisticsProvider(dto));
    }

    @PutMapping("/logistics-providers/{id}")
    public ResponseEntity<UserResponseDTO> updateLogisticsProvider(@PathVariable Long id,
                                                                   @RequestBody LogisticsProviderDto dto) {
        return ResponseEntity.ok(logisticsProviderService.updateLogisticsProvider(id, dto));
    }

    @DeleteMapping("/logistics-providers/{id}")
    public ResponseEntity<Void> deleteLogisticsProvider(@PathVariable Long id) {
        logisticsProviderService.deleteLogisticsProvider(id);
        return ResponseEntity.ok().build();
    }

    // --- Produce ---

    @GetMapping("/produce")
    public ResponseEntity<List<ProduceDto>> getAllProduce() {
        return ResponseEntity.ok(produceService.getAllProduce());
    }

    @GetMapping("/produce/{id}")
    public ResponseEntity<ProduceDto> getProduce(@PathVariable Long id) {
        return ResponseEntity.ok(produceService.getProduce(id));
    }

    @PostMapping("/produce")
    public ResponseEntity<ProduceDto> createProduce(@RequestBody CreateProduceDTO dto) {
        return ResponseEntity.ok(produceService.createProduce(dto));
    }

    @PutMapping("/produce/{id}")
    public ResponseEntity<ProduceDto> updateProduce(@PathVariable Long id, @RequestBody CreateProduceDTO dto) {
        return ResponseEntity.ok(produceService.updateProduce(id, dto));
    }

    @DeleteMapping("/produce/{id}")
    public ResponseEntity<Void> deleteProduce(@PathVariable Long id) {
        produceService.deleteProduce(id);
        return ResponseEntity.ok().build();
    }

    // --- Vehicles ---

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<VehicleDto> createVehicle(@RequestBody VehicleDto dto) {
        return ResponseEntity.ok(vehicleService.createVehicle(dto));
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Long id, @RequestBody VehicleDto dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }

    // --- Orders ---

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody CreateOrderDTO dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody CreateOrderDTO dto) {
        return ResponseEntity.ok(orderService.updateOrder(id, dto));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    // --- Order items ---

    @GetMapping("/order-items")
    public ResponseEntity<List<OrderItemResponseDTO>> getAllOrderItems() {
        return ResponseEntity.ok(orderItemService.getAllOrders());
    }

    @GetMapping("/order-items/{id}")
    public ResponseEntity<OrderItemResponseDTO> getOrderItem(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(id));
    }

    @PostMapping("/order-items")
    public ResponseEntity<OrderItemResponseDTO> createOrderItem(@RequestBody OrderItemDto dto) {
        return ResponseEntity.ok(orderItemService.createOrderItem(dto));
    }

    @PutMapping("/order-items/{id}")
    public ResponseEntity<OrderItemResponseDTO> updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDto dto) {
        return ResponseEntity.ok(orderItemService.updateOrderItem(id, dto));
    }

    @DeleteMapping("/order-items/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.ok().build();
    }

    // --- Reviews ---

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @RequestBody ReviewDto dto) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }

    // --- Logistics requests ---

    @GetMapping("/logistics")
    public ResponseEntity<List<LogisticsRequestDto>> getAllLogisticsRequests() {
        return ResponseEntity.ok(logisticsService.getAllLogisticsProviders());
    }

    @GetMapping("/logistics/{id}")
    public ResponseEntity<LogisticsRequestDto> getLogisticsRequest(@PathVariable Long id) {
        return ResponseEntity.ok(logisticsService.getLogisticsRequestById(id));
    }

    @PostMapping("/logistics")
    public ResponseEntity<LogisticsRequestDto> createLogisticsRequest(@RequestBody LogisticsRequestCreateDTO dto) {
        return ResponseEntity.ok(logisticsService.createLogisticsRequest(dto));
    }

    @PutMapping("/logistics/{id}")
    public ResponseEntity<LogisticsRequestDto> updateLogisticsRequest(@PathVariable Long id,
                                                                     @RequestBody LogisticsRequestCreateDTO dto) {
        return ResponseEntity.ok(logisticsService.updateLogisticsRequest(id, dto));
    }

    @DeleteMapping("/logistics/{id}")
    public ResponseEntity<Void> deleteLogisticsRequest(@PathVariable Long id) {
        logisticsService.deleteLogisticsRequest(id);
        return ResponseEntity.ok().build();
    }

    // --- Subscriptions ---

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/subscriptions/buyer/{buyerId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptionsByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByBuyer(buyerId));
    }

    @GetMapping("/subscriptions/farmer/{farmerId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptionsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByFarmer(farmerId));
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(@RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.createSubscription(dto));
    }

    @PutMapping("/subscriptions/{id}")
    public ResponseEntity<SubscriptionResponseDTO> updateSubscription(@PathVariable Long id,
                                                                      @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @PostMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<SubscriptionResponseDTO> cancelSubscription(@PathVariable Long id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    // --- Dispute reports ---

    @GetMapping("/dispute-reports")
    public ResponseEntity<List<DisputeReportResponseDTO>> getDisputeReports(
            @RequestParam(required = false) Boolean attendedTo) {
        return ResponseEntity.ok(disputeReportService.getAllReports(attendedTo));
    }

    @GetMapping("/dispute-reports/{id}")
    public ResponseEntity<DisputeReportResponseDTO> getDisputeReport(@PathVariable Long id) {
        return ResponseEntity.ok(disputeReportService.getReportById(id));
    }

    @PutMapping("/dispute-reports/{id}/attended")
    public ResponseEntity<DisputeReportResponseDTO> markDisputeReportAttended(@PathVariable Long id,
                                                                              @RequestParam boolean attendedTo) {
        return ResponseEntity.ok(disputeReportService.updateAttendedTo(id, attendedTo));
    }

    @DeleteMapping("/dispute-reports/{id}")
    public ResponseEntity<Void> deleteDisputeReport(@PathVariable Long id) {
        disputeReportService.deleteReport(id);
        return ResponseEntity.ok().build();
    }

    // --- Transactions ---

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) com.munashechipanga.eharvest.enums.TransactionType type,
            @RequestParam(required = false) com.munashechipanga.eharvest.enums.Currency currency
    ) {
        TransactionFilter filter = new TransactionFilter();
        filter.setStatus(status);
        filter.setType(type);
        filter.setCurrency(currency);
        List<TransactionHistory> txns = transactionRepository.findAll(TransactionSpecifications.withFilters(filter));
        return ResponseEntity.ok(txns.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    private TransactionDto mapToDto(TransactionHistory txn) {
        TransactionDto dto = new TransactionDto();
        dto.setId(txn.getId());
        dto.setTransactionReference(txn.getTransactionReference());
        dto.setTransactionDate(txn.getTransactionDate());
        dto.setAmount(txn.getAmount());
        dto.setStatus(txn.getStatus());
        dto.setBuyer(txn.getBuyer());
        dto.setFarmer(txn.getFarmer());
        dto.setOrder(txn.getOrder());
        dto.setUser(txn.getUser());
        dto.setCurrency(txn.getCurrency());
        dto.setType(txn.getType());
        dto.setProvider(txn.getProvider());
        dto.setProviderReference(txn.getProviderReference());
        return dto;
    }
}
