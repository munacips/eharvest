package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.SubscriptionDto;
import com.munashechipanga.eharvest.dtos.SubscriptionItemDto;
import com.munashechipanga.eharvest.dtos.response.SubscriptionResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.OrderItem;
import com.munashechipanga.eharvest.entities.Produce;
import com.munashechipanga.eharvest.entities.Subscription;
import com.munashechipanga.eharvest.entities.SubscriptionItem;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.LogisticsStatus;
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.BuyerRepository;
import com.munashechipanga.eharvest.repositories.FarmerRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderItemRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.ProduceRepository;
import com.munashechipanga.eharvest.repositories.SubscriptionRepository;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String ACTIVE = "ACTIVE";
    private static final String PAUSED = "PAUSED";
    private static final String CANCELLED = "CANCELLED";

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    BuyerRepository buyerRepository;

    @Autowired
    FarmerRepository farmerRepository;

    @Autowired
    ProduceRepository produceRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

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
    public SubscriptionResponseDTO createSubscription(SubscriptionDto dto) {
        Buyer buyer = buyerRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + dto.getBuyerId()));
        Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + dto.getFarmerId()));

        Subscription subscription = new Subscription();
        LocalDateTime startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDateTime.now();
        subscription.setBuyer(buyer);
        subscription.setFarmer(farmer);
        subscription.setStatus(ACTIVE);
        subscription.setFrequency(normalizeFrequency(dto.getFrequency()));
        subscription.setStartDate(startDate);
        subscription.setNextDeliveryDate(computeNextDeliveryDate(startDate, subscription.getFrequency()));
        subscription.setRequiresLogistics(dto.getRequiresLogistics() != null ? dto.getRequiresLogistics() : false);
        subscription.setPickupAddress(dto.getPickupAddress());
        subscription.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.USD);
        setSubscriptionItems(subscription, dto.getItems());

        Subscription saved = subscriptionRepository.save(subscription);
        notifySubscriptionParties(saved, "Subscription created",
                "Subscription " + saved.getId() + " has been created.");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponseDTO updateSubscription(Long id, SubscriptionDto dto) {
        Subscription subscription = getSubscriptionEntity(id);

        if (dto.getBuyerId() != null) {
            Buyer buyer = buyerRepository.findById(dto.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + dto.getBuyerId()));
            subscription.setBuyer(buyer);
        }
        if (dto.getFarmerId() != null) {
            Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + dto.getFarmerId()));
            subscription.setFarmer(farmer);
        }
        if (dto.getFrequency() != null) {
            subscription.setFrequency(normalizeFrequency(dto.getFrequency()));
            subscription.setNextDeliveryDate(computeNextDeliveryDate(
                    subscription.getStartDate() != null ? subscription.getStartDate() : LocalDateTime.now(),
                    subscription.getFrequency()));
        }
        if (dto.getStatus() != null) subscription.setStatus(normalizeStatus(dto.getStatus()));
        if (dto.getRequiresLogistics() != null) subscription.setRequiresLogistics(dto.getRequiresLogistics());
        if (dto.getPickupAddress() != null) subscription.setPickupAddress(dto.getPickupAddress());
        if (dto.getCurrency() != null) subscription.setCurrency(dto.getCurrency());
        if (dto.getStartDate() != null) {
            subscription.setStartDate(dto.getStartDate());
            subscription.setNextDeliveryDate(computeNextDeliveryDate(dto.getStartDate(), subscription.getFrequency()));
        }
        if (dto.getItems() != null) {
            subscription.getItems().clear();
            setSubscriptionItems(subscription, dto.getItems());
        }

        return mapToDto(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponseDTO getSubscriptionById(Long id) {
        return mapToDto(getSubscriptionEntity(id));
    }

    @Override
    @Transactional
    public void cancelSubscription(Long id) {
        Subscription subscription = getSubscriptionEntity(id);
        subscription.setStatus(CANCELLED);
        Subscription saved = subscriptionRepository.save(subscription);
        notifySubscriptionParties(saved, "Subscription cancelled",
                "Subscription " + saved.getId() + " has been cancelled.");
    }

    @Override
    @Transactional
    public void pauseSubscription(Long id) {
        Subscription subscription = getSubscriptionEntity(id);
        subscription.setStatus(PAUSED);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void resumeSubscription(Long id) {
        Subscription subscription = getSubscriptionEntity(id);
        subscription.setStatus(ACTIVE);
        subscription.setNextDeliveryDate(computeNextDeliveryDate(LocalDateTime.now(), subscription.getFrequency()));
        subscriptionRepository.save(subscription);
    }

    @Override
    public List<SubscriptionResponseDTO> getSubscriptionsByBuyer(Long buyerId) {
        return subscriptionRepository.findByBuyer_Id(buyerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponseDTO> getSubscriptionsByFarmer(Long farmerId) {
        return subscriptionRepository.findByFarmer_Id(farmerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processSubscription(Long subscriptionId) {
        Subscription subscription = getSubscriptionEntity(subscriptionId);
        if (!ACTIVE.equals(subscription.getStatus())) {
            return;
        }

        Double totalAmount = computeTotalAmount(subscription);
        Currency currency = subscription.getCurrency() != null ? subscription.getCurrency() : Currency.USD;
        Buyer buyer = subscription.getBuyer();
        Farmer farmer = subscription.getFarmer();

        if (!hasSufficientBalance(buyer, currency, totalAmount)) {
            subscription.setStatus(PAUSED);
            subscriptionRepository.save(subscription);
            notificationService.sendPaymentUpdate(buyer, "Subscription paused",
                    "Subscription " + subscription.getId() + " was paused because your balance is insufficient.");
            return;
        }

        subtractBalance(buyer, currency, totalAmount);

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setBuyer(buyer);
        order.setFarmer(farmer);
        order.setStatus(OrderStatus.PENDING.name());
        order.setTotalAmount(totalAmount);
        order.setCurrency(currency);
        order.setEscrowHeld(true);
        order.setEscrowReleased(false);
        order.setEscrowAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        for (SubscriptionItem subscriptionItem : subscription.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduce(subscriptionItem.getProduce());
            orderItem.setQuantity(toOrderQuantity(subscriptionItem.getQuantity()));
            orderItem.setPrice(subscriptionItem.getUnitPrice());
            orderItemRepository.save(orderItem);
        }

        createEscrowTransaction(savedOrder, buyer, null, totalAmount, currency, TransactionType.ESCROW_HOLD);

        if (Boolean.TRUE.equals(subscription.getRequiresLogistics())) {
            LogisticsRequest logisticsRequest = new LogisticsRequest();
            logisticsRequest.setPickupLocation(farmer != null ? farmer.getFarmLocation() : null);
            logisticsRequest.setDeliveryLocation(buyer != null ? buyer.getAddress() : null);
            logisticsRequest.setStatus(LogisticsStatus.SEARCHING.name());
            logisticsRequest.setCost(0.0);
            logisticsRequest.setEscrowHeld(false);
            logisticsRequest.setEscrowReleased(false);
            logisticsRequest.setOrder(savedOrder);
            LogisticsRequest savedLogistics = logisticsRepository.save(logisticsRequest);
            savedOrder.setLogisticsRequest(savedLogistics);
            orderRepository.save(savedOrder);
        }

        subscription.setNextDeliveryDate(computeNextDeliveryDate(subscription.getNextDeliveryDate(), subscription.getFrequency()));
        subscriptionRepository.save(subscription);

        notificationService.sendOrderUpdate(buyer, "Subscription order created",
                "Order " + savedOrder.getId() + " was created from subscription " + subscription.getId() + ".");
        notificationService.sendOrderUpdate(farmer, "Subscription order created",
                "Order " + savedOrder.getId() + " was created from subscription " + subscription.getId() + ".");
    }

    private void setSubscriptionItems(Subscription subscription, List<SubscriptionItemDto> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) {
            throw new IllegalArgumentException("Subscription must include at least one item");
        }
        for (SubscriptionItemDto itemDto : itemDtos) {
            if (itemDto.getProduceId() == null) {
                throw new IllegalArgumentException("Produce id is required");
            }
            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than zero");
            }

            Produce produce = produceRepository.findById(itemDto.getProduceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produce not found with id: " + itemDto.getProduceId()));
            Double unitPrice = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : produce.getPrice();
            if (unitPrice == null || unitPrice <= 0) {
                throw new IllegalArgumentException("Item unit price must be greater than zero");
            }

            SubscriptionItem item = new SubscriptionItem();
            item.setSubscription(subscription);
            item.setProduce(produce);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(unitPrice);
            subscription.getItems().add(item);
        }
    }

    private SubscriptionResponseDTO mapToDto(Subscription subscription) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setId(subscription.getId());
        dto.setBuyer(subscription.getBuyer());
        dto.setFarmer(subscription.getFarmer());
        dto.setBuyerId(subscription.getBuyer() != null ? subscription.getBuyer().getId() : null);
        dto.setFarmerId(subscription.getFarmer() != null ? subscription.getFarmer().getId() : null);
        dto.setFrequency(subscription.getFrequency());
        dto.setStatus(subscription.getStatus());
        dto.setRequiresLogistics(subscription.getRequiresLogistics());
        dto.setPickupAddress(subscription.getPickupAddress());
        dto.setCurrency(subscription.getCurrency());
        dto.setStartDate(subscription.getStartDate());
        dto.setNextDeliveryDate(subscription.getNextDeliveryDate());
        dto.setItems(subscription.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList()));
        dto.setTotalAmount(computeTotalAmount(subscription));
        return dto;
    }

    private SubscriptionItemDto mapItemToDto(SubscriptionItem item) {
        SubscriptionItemDto dto = new SubscriptionItemDto();
        dto.setId(item.getId());
        dto.setProduceId(item.getProduce() != null ? item.getProduce().getId() : null);
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        return dto;
    }

    private Subscription getSubscriptionEntity(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    private void notifySubscriptionParties(Subscription subscription, String title, String message) {
        if (subscription.getBuyer() != null) {
            notificationService.sendOrderUpdate(subscription.getBuyer(), title, message);
        }
        if (subscription.getFarmer() != null) {
            notificationService.sendOrderUpdate(subscription.getFarmer(), title, message);
        }
    }

    private LocalDateTime computeNextDeliveryDate(LocalDateTime from, String frequency) {
        LocalDateTime base = from != null ? from : LocalDateTime.now();
        String normalizedFrequency = normalizeFrequency(frequency);
        return switch (normalizedFrequency) {
            case "WEEKLY" -> base.plusDays(7);
            case "BIWEEKLY" -> base.plusDays(14);
            case "MONTHLY" -> base.plusMonths(1);
            default -> throw new IllegalArgumentException("Unsupported subscription frequency: " + frequency);
        };
    }

    private Double computeTotalAmount(Subscription subscription) {
        return subscription.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }

    private String normalizeFrequency(String frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Subscription frequency is required");
        }
        String normalized = frequency.trim().toUpperCase(Locale.ROOT);
        if (!"WEEKLY".equals(normalized) && !"BIWEEKLY".equals(normalized) && !"MONTHLY".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported subscription frequency: " + frequency);
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!ACTIVE.equals(normalized) && !PAUSED.equals(normalized) && !CANCELLED.equals(normalized)) {
            throw new IllegalArgumentException("Unsupported subscription status: " + status);
        }
        return normalized;
    }

    private boolean hasSufficientBalance(User user, Currency currency, Double amount) {
        if (user == null || amount == null) return false;
        if (currency == Currency.USD) {
            double current = user.getUsdBalance() != null ? user.getUsdBalance() : 0.0;
            return current >= amount;
        }
        double current = user.getZigBalance() != null ? user.getZigBalance() : 0.0;
        return current >= amount;
    }

    private Integer toOrderQuantity(Double quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Order item quantity must be greater than zero");
        }
        if (quantity % 1 != 0) {
            throw new IllegalArgumentException("Order item quantity must be a whole number");
        }
        return quantity.intValue();
    }

    private void createEscrowTransaction(Order order, Buyer buyer, Farmer farmer, Double amount, Currency currency, TransactionType type) {
        TransactionHistory txn = new TransactionHistory();
        txn.setTransactionDate(LocalDateTime.now());
        txn.setTransactionReference("SUB-ESCROW-" + System.currentTimeMillis());
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
