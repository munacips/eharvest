package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.BuyerRepository;
import com.munashechipanga.eharvest.repositories.FarmerRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.LogisticsType;
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    BuyerRepository buyerRepository;

    @Autowired
    FarmerRepository farmerRepository;

    @Autowired
    LogisticsRepository logisticsRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(CreateOrderDTO dto) {
        if (dto.getTotalAmount() == null || dto.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Order total amount must be greater than zero");
        }
        if (dto.getEscrowAmount() != null && dto.getEscrowAmount() <= 0) {
            throw new IllegalArgumentException("Escrow amount must be greater than zero");
        }

        LocalDateTime dateTime = LocalDateTime.now();

        Order order = new Order();

        order.setOrderDate(dateTime);
        order.setEscrowReleased(dto.getEscrowReleased() != null ? dto.getEscrowReleased() : false);
        order.setEscrowHeld(false);
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : OrderStatus.PENDING.name());
        order.setTotalAmount(dto.getTotalAmount());
        order.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.USD);
        order.setLogisticsType(dto.getLogisticsType() != null ? dto.getLogisticsType() : LogisticsType.THIRD_PARTY);
        order.setEscrowAmount(dto.getEscrowAmount() != null ? dto.getEscrowAmount() : dto.getTotalAmount());

        if (dto.getBuyerId() != null) {
            Buyer buyer = buyerRepository.findById(dto.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + dto.getBuyerId()));
            order.setBuyer(buyer);
        }

        if (dto.getFarmerId() != null) {
            Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + dto.getFarmerId()));
            order.setFarmer(farmer);
        }

        if (dto.getLogisticsRequestId() != null) {
            LogisticsRequest logisticsRequest = logisticsRepository.findById(dto.getLogisticsRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("LogisticsRequest not found with id: " + dto.getLogisticsRequestId()));
            order.setLogisticsRequest(logisticsRequest);
        }

        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getBuyer() != null) {
            notificationService.sendOrderUpdate(savedOrder.getBuyer(), "Order created",
                    "Your order " + savedOrder.getId() + " has been created.");
        }
        if (savedOrder.getFarmer() != null) {
            notificationService.sendOrderUpdate(savedOrder.getFarmer(), "New order",
                    "You have a new order " + savedOrder.getId() + ".");
        }

        return mapToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrder(Long id, CreateOrderDTO dto) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String previousStatus = order.getStatus();

        if(dto.getStatus() != null) order.setStatus(dto.getStatus());
        if(dto.getEscrowReleased() != null) order.setEscrowReleased(dto.getEscrowReleased());

        if(dto.getBuyerId() != null) {
            Buyer buyer = buyerRepository.findById(dto.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + dto.getBuyerId()));
            order.setBuyer(buyer);
        }

        if(dto.getFarmerId() != null) {
            Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + dto.getFarmerId()));
            order.setFarmer(farmer);
        }

        if (dto.getLogisticsRequestId() != null) {
            LogisticsRequest logisticsRequest = logisticsRepository.findById(dto.getLogisticsRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("LogisticsRequest not found with id: " + dto.getLogisticsRequestId()));
            order.setLogisticsRequest(logisticsRequest);
        }

        if(dto.getTotalAmount() != null) {
            if (dto.getTotalAmount() <= 0) {
                throw new IllegalArgumentException("Order total amount must be greater than zero");
            }
            order.setTotalAmount(dto.getTotalAmount());
        }
        if(dto.getCurrency() != null) order.setCurrency(dto.getCurrency());
        if(dto.getEscrowAmount() != null) {
            if (dto.getEscrowAmount() <= 0) {
                throw new IllegalArgumentException("Escrow amount must be greater than zero");
            }
            order.setEscrowAmount(dto.getEscrowAmount());
        }

        reconcileOrderOutcomeStats(order, previousStatus, order.getStatus());
        Order updatedOrder = orderRepository.save(order);

        return mapToDto(updatedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDto(order);
    }

    @Override
    public List<OrderResponseDTO> getOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByFarmerId(Long farmerId) {
        return orderRepository.findByFarmer_Id(farmerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByBuyerId(Long buyerId) {
        return orderRepository.findByBuyer_Id(buyerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO acceptOrder(Long id) {
        Order order = getOrderEntity(id);
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be pending to accept");
        }
        if (getLogisticsType(order) == LogisticsType.BUYER_PICKUP) {
            order.setEscrowAmount(order.getTotalAmount());
            holdEscrowForOrder(order);
        }
        order.setStatus(OrderStatus.ACCEPTED.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Order accepted", "Order " + saved.getId() + " was accepted.");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO rejectOrder(Long id, String reason) {
        Order order = getOrderEntity(id);
        String previousStatus = order.getStatus();
        order.setStatus(OrderStatus.REJECTED.name());
        reconcileOrderOutcomeStats(order, previousStatus, order.getStatus());
        Order saved = orderRepository.save(order);
        if (saved.getEscrowHeld() != null && saved.getEscrowHeld() && !Boolean.TRUE.equals(saved.getEscrowReleased())) {
            refundEscrow(saved);
        }
        notifyOrderParties(saved, "Order rejected", "Order " + saved.getId() + " was rejected. " + (reason != null ? reason : ""));
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long id, String reason) {
        Order order = getOrderEntity(id);
        if (isTerminalSuccessStatus(order.getStatus())) {
            throw new IllegalArgumentException("Delivered orders cannot be cancelled");
        }
        if (isTerminalFailureStatus(order.getStatus()) && OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            return mapToDto(order);
        }

        String previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED.name());
        reconcileOrderOutcomeStats(order, previousStatus, order.getStatus());
        Order saved = orderRepository.save(order);
        if (saved.getEscrowHeld() != null && saved.getEscrowHeld() && !Boolean.TRUE.equals(saved.getEscrowReleased())) {
            refundEscrow(saved);
        }
        notifyOrderParties(saved, "Order cancelled", "Order " + saved.getId() + " was cancelled. " + (reason != null ? reason : ""));
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO holdEscrow(Long id) {
        Order order = getOrderEntity(id);
        if (!(OrderStatus.PENDING.name().equals(order.getStatus()) || OrderStatus.ACCEPTED.name().equals(order.getStatus()))) {
            throw new IllegalArgumentException("Escrow can only be held for pending or accepted orders");
        }
        if (Boolean.TRUE.equals(order.getEscrowHeld())) {
            return mapToDto(order);
        }
        holdEscrowForOrder(order);
        if (order.getBuyer() != null) {
            notificationService.sendPaymentUpdate(order.getBuyer(), "Escrow held",
                    "Funds for order " + order.getId() + " have been held in escrow.");
        }
        return mapToDto(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmDeliveryStarted(Long id) {
        Order order = getOrderEntity(id);
        LogisticsType logisticsType = getLogisticsType(order);
        if (logisticsType == LogisticsType.BUYER_PICKUP) {
            throw new IllegalArgumentException("Delivery start not applicable for buyer pickup");
        }
        if (logisticsType == LogisticsType.FARMER_DELIVERY) {
            if (!OrderStatus.IN_TRANSIT.name().equals(order.getStatus())) {
                throw new IllegalArgumentException("Farmer delivery starts after buyer accepts transport fee");
            }
            return mapToDto(order);
        }
        if (!OrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be accepted before delivery starts");
        }
        order.setStatus(OrderStatus.IN_TRANSIT.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Delivery started", "Delivery started for order " + saved.getId() + ".");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmDelivery(Long id) {
        Order order = getOrderEntity(id);
        LogisticsType logisticsType = getLogisticsType(order);
        boolean validStatus = logisticsType == LogisticsType.BUYER_PICKUP
                ? OrderStatus.ACCEPTED.name().equals(order.getStatus())
                : OrderStatus.IN_TRANSIT.name().equals(order.getStatus());
        if (!validStatus) {
            throw new IllegalArgumentException("Order must be in transit before delivery confirmation");
        }
        String previousStatus = order.getStatus();
        order.setStatus(OrderStatus.DELIVERED.name());
        if (Boolean.TRUE.equals(order.getEscrowHeld()) && !Boolean.TRUE.equals(order.getEscrowReleased())) {
            releaseEscrow(order);
        }
        reconcileOrderOutcomeStats(order, previousStatus, order.getStatus());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Order delivered", "Order " + saved.getId() + " marked as delivered.");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO proposeTransportFee(Long id, Double fee) {
        Order order = getOrderEntity(id);
        if (getLogisticsType(order) != LogisticsType.FARMER_DELIVERY) {
            throw new IllegalArgumentException("Transport fee proposal is only valid for farmer delivery");
        }
        if (!OrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be accepted before proposing transport fee");
        }
        if (fee == null || fee <= 0) {
            throw new IllegalArgumentException("Transport fee must be greater than zero");
        }
        order.setTransportFee(fee);
        order.setStatus(OrderStatus.AWAITING_TRANSPORT_FEE_APPROVAL.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Transport fee proposed", "Transport fee proposed for order " + saved.getId() + ".");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO acceptTransportFee(Long id) {
        Order order = getOrderEntity(id);
        if (getLogisticsType(order) != LogisticsType.FARMER_DELIVERY) {
            throw new IllegalArgumentException("Transport fee approval is only valid for farmer delivery");
        }
        if (!OrderStatus.AWAITING_TRANSPORT_FEE_APPROVAL.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be awaiting transport fee approval");
        }
        if (order.getTransportFee() == null || order.getTransportFee() <= 0) {
            throw new IllegalArgumentException("Transport fee must be greater than zero");
        }
        order.setEscrowAmount(order.getTotalAmount() + order.getTransportFee());
        holdEscrowForOrder(order);
        order.setStatus(OrderStatus.IN_TRANSIT.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Transport fee accepted", "Transport fee accepted for order " + saved.getId() + ".");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public OrderResponseDTO rejectTransportFee(Long id) {
        Order order = getOrderEntity(id);
        if (getLogisticsType(order) != LogisticsType.FARMER_DELIVERY) {
            throw new IllegalArgumentException("Transport fee rejection is only valid for farmer delivery");
        }
        if (!OrderStatus.AWAITING_TRANSPORT_FEE_APPROVAL.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be awaiting transport fee approval");
        }
        order.setTransportFee(null);
        order.setStatus(OrderStatus.ACCEPTED.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Transport fee rejected", "Transport fee rejected for order " + saved.getId() + ".");
        return mapToDto(saved);
    }

    private OrderResponseDTO mapToDto(Order order){
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setBuyer(order.getBuyer());
        dto.setFarmer(order.getFarmer());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setLogisticsRequest(order.getLogisticsRequest());
        dto.setLogisticsType(getLogisticsType(order));
        dto.setTransportFee(order.getTransportFee());
        dto.setEscrowReleased(order.getEscrowReleased());
        dto.setCurrency(order.getCurrency());
        dto.setEscrowHeld(order.getEscrowHeld());
        dto.setEscrowAmount(order.getEscrowAmount());

        return dto;
    }

    private Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void notifyOrderParties(Order order, String title, String message) {
        if (order.getBuyer() != null) {
            notificationService.sendOrderUpdate(order.getBuyer(), title, message);
        }
        if (order.getFarmer() != null) {
            notificationService.sendOrderUpdate(order.getFarmer(), title, message);
        }
    }

    private LogisticsType getLogisticsType(Order order) {
        return order.getLogisticsType() != null ? order.getLogisticsType() : LogisticsType.THIRD_PARTY;
    }

    private void holdEscrowForOrder(Order order) {
        if (Boolean.TRUE.equals(order.getEscrowHeld())) {
            return;
        }
        Buyer buyer = order.getBuyer();
        if (buyer == null) {
            throw new ResourceNotFoundException("Order has no buyer");
        }
        Currency currency = order.getCurrency() != null ? order.getCurrency() : Currency.USD;
        Double amount = order.getEscrowAmount() != null ? order.getEscrowAmount() : order.getTotalAmount();
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Escrow amount must be greater than zero");
        }
        subtractBalance(buyer, currency, amount);
        order.setEscrowHeld(true);
        orderRepository.save(order);
        createEscrowTransaction(order, buyer, null, amount, currency, TransactionType.ESCROW_HOLD);
    }

    private void releaseEscrow(Order order) {
        Farmer farmer = order.getFarmer();
        if (farmer == null) {
            throw new ResourceNotFoundException("Order has no farmer");
        }
        Currency currency = order.getCurrency() != null ? order.getCurrency() : Currency.USD;
        Double amount = order.getEscrowAmount() != null ? order.getEscrowAmount() : order.getTotalAmount();
        addBalance(farmer, currency, amount);
        order.setEscrowReleased(true);
        createEscrowTransaction(order, null, farmer, amount, currency, TransactionType.ESCROW_RELEASE);
        notificationService.sendPaymentUpdate(farmer, "Escrow released",
                "Funds for order " + order.getId() + " have been released.");
    }

    private void refundEscrow(Order order) {
        Buyer buyer = order.getBuyer();
        if (buyer == null) {
            throw new ResourceNotFoundException("Order has no buyer");
        }
        Currency currency = order.getCurrency() != null ? order.getCurrency() : Currency.USD;
        Double amount = order.getEscrowAmount() != null ? order.getEscrowAmount() : order.getTotalAmount();
        addBalance(buyer, currency, amount);
        order.setEscrowReleased(false);
        order.setEscrowHeld(false);
        createEscrowTransaction(order, buyer, null, amount, currency, TransactionType.REFUND);
        notificationService.sendPaymentUpdate(buyer, "Refund processed",
                "Refund for order " + order.getId() + " has been processed.");
    }

    private void createEscrowTransaction(Order order, Buyer buyer, Farmer farmer, Double amount, Currency currency, TransactionType type) {
        TransactionHistory txn = new TransactionHistory();
        txn.setTransactionDate(LocalDateTime.now());
        txn.setTransactionReference("ESCROW-" + System.currentTimeMillis());
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setOrder(order);
        txn.setBuyer(buyer);
        txn.setFarmer(farmer);
        txn.setUser(buyer != null ? buyer : farmer);
        txn.setCurrency(currency);
        txn.setType(type);
        txn.setProvider("IN_APP");
        transactionRepository.save(txn);
    }

    private void reconcileOrderOutcomeStats(Order order, String previousStatus, String newStatus) {
        if (previousStatus == null && newStatus == null) {
            return;
        }
        if (previousStatus != null && previousStatus.equals(newStatus)) {
            return;
        }

        if (isTerminalSuccessStatus(previousStatus) && !isTerminalSuccessStatus(newStatus)) {
            adjustSuccessfulCounts(order.getBuyer(), order.getFarmer(), -1);
        } else if (!isTerminalSuccessStatus(previousStatus) && isTerminalSuccessStatus(newStatus)) {
            adjustSuccessfulCounts(order.getBuyer(), order.getFarmer(), 1);
        }

        if (isTerminalFailureStatus(previousStatus) && !isTerminalFailureStatus(newStatus)) {
            adjustUnsuccessfulCounts(order.getBuyer(), order.getFarmer(), -1);
        } else if (!isTerminalFailureStatus(previousStatus) && isTerminalFailureStatus(newStatus)) {
            adjustUnsuccessfulCounts(order.getBuyer(), order.getFarmer(), 1);
        }
    }

    private boolean isTerminalSuccessStatus(String status) {
        return OrderStatus.DELIVERED.name().equals(status);
    }

    private boolean isTerminalFailureStatus(String status) {
        return OrderStatus.REJECTED.name().equals(status) || OrderStatus.CANCELLED.name().equals(status);
    }

    private void adjustSuccessfulCounts(Buyer buyer, Farmer farmer, int delta) {
        if (buyer != null) {
            buyer.setSuccessfulBuys(adjustCount(buyer.getSuccessfulBuys(), delta));
            buyerRepository.save(buyer);
        }
        if (farmer != null) {
            farmer.setSuccessfulSales(adjustCount(farmer.getSuccessfulSales(), delta));
            farmerRepository.save(farmer);
        }
    }

    private void adjustUnsuccessfulCounts(Buyer buyer, Farmer farmer, int delta) {
        if (buyer != null) {
            buyer.setUnsuccessfulBuys(adjustCount(buyer.getUnsuccessfulBuys(), delta));
            buyerRepository.save(buyer);
        }
        if (farmer != null) {
            farmer.setUnsuccessfulSales(adjustCount(farmer.getUnsuccessfulSales(), delta));
            farmerRepository.save(farmer);
        }
    }

    private int adjustCount(Integer currentValue, int delta) {
        int current = currentValue != null ? currentValue : 0;
        int updated = current + delta;
        return Math.max(updated, 0);
    }

    private void subtractBalance(User user, Currency currency, Double amount) {
        if (amount == null) return;
        if (currency == Currency.USD) {
            double current = user.getUsdBalance() != null ? user.getUsdBalance() : 0.0;
            if (current < amount) {
                throw new IllegalArgumentException("Insufficient USD balance");
            }
            user.setUsdBalance(current - amount);
        } else {
            double current = user.getZigBalance() != null ? user.getZigBalance() : 0.0;
            if (current < amount) {
                throw new IllegalArgumentException("Insufficient ZIG balance");
            }
            user.setZigBalance(current - amount);
        }
        userRepository.save(user);
    }

    private void addBalance(User user, Currency currency, Double amount) {
        if (amount == null) return;
        if (currency == Currency.USD) {
            double current = user.getUsdBalance() != null ? user.getUsdBalance() : 0.0;
            user.setUsdBalance(current + amount);
        } else {
            double current = user.getZigBalance() != null ? user.getZigBalance() : 0.0;
            user.setZigBalance(current + amount);
        }
        userRepository.save(user);
    }
}
