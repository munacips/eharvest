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
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public OrderResponseDTO createOrder(CreateOrderDTO dto) {

        LocalDateTime dateTime = LocalDateTime.now();

        Order order = new Order();

        order.setOrderDate(dateTime);
        order.setEscrowReleased(dto.getEscrowReleased() != null ? dto.getEscrowReleased() : false);
        order.setEscrowHeld(false);
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : OrderStatus.PENDING.name());
        order.setTotalAmount(dto.getTotalAmount());
        order.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.USD);
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
    public OrderResponseDTO updateOrder(Long id, CreateOrderDTO dto) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

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

        if(dto.getTotalAmount() != null) order.setTotalAmount(dto.getTotalAmount());
        if(dto.getCurrency() != null) order.setCurrency(dto.getCurrency());
        if(dto.getEscrowAmount() != null) order.setEscrowAmount(dto.getEscrowAmount());

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
    public OrderResponseDTO acceptOrder(Long id) {
        Order order = getOrderEntity(id);
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be pending to accept");
        }
        order.setStatus(OrderStatus.ACCEPTED.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Order accepted", "Order " + saved.getId() + " was accepted.");
        return mapToDto(saved);
    }

    @Override
    public OrderResponseDTO rejectOrder(Long id, String reason) {
        Order order = getOrderEntity(id);
        order.setStatus(OrderStatus.REJECTED.name());
        Order saved = orderRepository.save(order);
        if (saved.getEscrowHeld() != null && saved.getEscrowHeld() && !Boolean.TRUE.equals(saved.getEscrowReleased())) {
            refundEscrow(saved);
        }
        notifyOrderParties(saved, "Order rejected", "Order " + saved.getId() + " was rejected. " + (reason != null ? reason : ""));
        return mapToDto(saved);
    }

    @Override
    public OrderResponseDTO holdEscrow(Long id) {
        Order order = getOrderEntity(id);
        if (!(OrderStatus.PENDING.name().equals(order.getStatus()) || OrderStatus.ACCEPTED.name().equals(order.getStatus()))) {
            throw new IllegalArgumentException("Escrow can only be held for pending or accepted orders");
        }
        if (Boolean.TRUE.equals(order.getEscrowHeld())) {
            return mapToDto(order);
        }
        Buyer buyer = order.getBuyer();
        if (buyer == null) {
            throw new ResourceNotFoundException("Order has no buyer");
        }
        Currency currency = order.getCurrency() != null ? order.getCurrency() : Currency.USD;
        Double amount = order.getEscrowAmount() != null ? order.getEscrowAmount() : order.getTotalAmount();
        subtractBalance(buyer, currency, amount);
        order.setEscrowHeld(true);
        orderRepository.save(order);
        createEscrowTransaction(order, buyer, null, amount, currency, TransactionType.ESCROW_HOLD);
        notificationService.sendPaymentUpdate(buyer, "Escrow held",
                "Funds for order " + order.getId() + " have been held in escrow.");
        return mapToDto(order);
    }

    @Override
    public OrderResponseDTO confirmDeliveryStarted(Long id) {
        Order order = getOrderEntity(id);
        if (!OrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be accepted before delivery starts");
        }
        order.setStatus(OrderStatus.IN_TRANSIT.name());
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Delivery started", "Delivery started for order " + saved.getId() + ".");
        return mapToDto(saved);
    }

    @Override
    public OrderResponseDTO confirmDelivery(Long id) {
        Order order = getOrderEntity(id);
        if (!OrderStatus.IN_TRANSIT.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Order must be in transit before delivery confirmation");
        }
        order.setStatus(OrderStatus.DELIVERED.name());
        if (Boolean.TRUE.equals(order.getEscrowHeld()) && !Boolean.TRUE.equals(order.getEscrowReleased())) {
            releaseEscrow(order);
        }
        Order saved = orderRepository.save(order);
        notifyOrderParties(saved, "Order delivered", "Order " + saved.getId() + " marked as delivered.");
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
        txn.setCurrency(currency);
        txn.setType(type);
        txn.setProvider("IN_APP");
        transactionRepository.save(txn);
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
