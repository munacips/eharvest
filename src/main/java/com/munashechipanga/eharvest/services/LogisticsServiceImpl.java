package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.dtos.request.LogisticsRequestCreateDTO;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.LogisticsProviderRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.enums.LogisticsStatus;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    LogisticsRepository logisticsRepository;

    @Autowired
    LogisticsProviderRepository providerRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationService notificationService;

    @Override
    public LogisticsRequestDto getLogisticsRequestById(Long id) {
        LogisticsRequest request = logisticsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        return mapToDto(request);
    }

    @Override
    public LogisticsRequestDto createLogisticsRequest(LogisticsRequestCreateDTO dto) {

        LogisticsRequest request = new LogisticsRequest();
        request.setPickupLocation( dto.getPickupLocation());
        request.setDeliveryLocation(dto.getDeliveryLocation());
        request.setStatus(LogisticsStatus.SEARCHING.name());
        request.setCost(dto.getCost());
        request.setEscrowHeld(dto.getEscrowHeld() != null ? dto.getEscrowHeld() : false);
        request.setEscrowReleased(dto.getEscrowReleased() != null ? dto.getEscrowReleased() : false);


        if(dto.getAssignedProvider() != null) {

            LogisticsProvider provider = providerRepository.findById(dto.getAssignedProvider())
                    .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

            request.setAssignedProvider(provider);
        }

        if(dto.getOrder() != null){
            Order order = orderRepository.findById(dto.getOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            request.setOrder(order);
        }


        LogisticsRequest newRequest = logisticsRepository.save(request);

        return mapToDto(newRequest);
    }

    @Override
    public void deleteLogisticsRequest(Long id) {
        logisticsRepository.deleteById(id);
    }

    @Override
    public LogisticsRequestDto updateLogisticsRequest(Long id, LogisticsRequestCreateDTO dto) {
        LogisticsRequest request = logisticsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));

        if(dto.getPickupLocation() != null) request.setPickupLocation(dto.getPickupLocation());
        if(dto.getDeliveryLocation() != null) request.setDeliveryLocation(dto.getDeliveryLocation());
        if(dto.getStatus() != null)  request.setStatus(dto.getStatus());
        if(dto.getCost() != null)  request.setCost(dto.getCost());
        if(dto.getEscrowHeld() != null) request.setEscrowHeld(dto.getEscrowHeld());
        if(dto.getEscrowReleased() != null) request.setEscrowReleased(dto.getEscrowReleased());

        if(dto.getAssignedProvider() != null) {

            LogisticsProvider provider = providerRepository.findById(dto.getAssignedProvider())
                    .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

            request.setAssignedProvider(provider);
        }

        if(dto.getOrder() != null){
            Order order = orderRepository.findById(dto.getOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            request.setOrder(order);
        }

        LogisticsRequest newRequest = logisticsRepository.save(request);

        return mapToDto(newRequest);
    }

    @Override
    public List<LogisticsRequestDto> getAllLogisticsProviders() {
        return logisticsRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LogisticsRequestDto getLogisticsRequestByOrderId(Long id) {
        LogisticsRequest request = logisticsRepository.findByOrder_Id(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        return mapToDto(request);
    }

    @Override
    @Transactional
    public LogisticsRequestDto acceptRequest(Long requestId, Long providerId) {
        LogisticsRequest request = logisticsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        LogisticsProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        request.setAssignedProvider(provider);
        request.setStatus(LogisticsStatus.ASSIGNED.name());
        LogisticsRequest saved = logisticsRepository.save(request);
        if (!Boolean.TRUE.equals(saved.getEscrowHeld())) {
            holdLogisticsEscrow(saved);
            saved = logisticsRepository.save(saved);
        }
        notifyOrder(saved, "Logistics accepted", "Your logistics request has been accepted.");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public LogisticsRequestDto rejectRequest(Long requestId, Long providerId) {
        LogisticsRequest request = logisticsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        if (request.getAssignedProvider() != null && !request.getAssignedProvider().getId().equals(providerId)) {
            throw new IllegalArgumentException("Request assigned to another provider");
        }
        request.setStatus(LogisticsStatus.REJECTED.name());
        if (Boolean.TRUE.equals(request.getEscrowHeld()) && !Boolean.TRUE.equals(request.getEscrowReleased())) {
            refundLogisticsEscrow(request);
        }
        LogisticsRequest saved = logisticsRepository.save(request);
        notifyOrder(saved, "Logistics rejected", "Your logistics request has been rejected.");
        return mapToDto(saved);
    }

    @Override
    public LogisticsRequestDto markInTransit(Long requestId) {
        LogisticsRequest request = logisticsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        if (!Boolean.TRUE.equals(request.getEscrowHeld())) {
            throw new IllegalArgumentException("Logistics escrow must be held before delivery starts");
        }
        request.setStatus(LogisticsStatus.IN_TRANSIT.name());
        LogisticsRequest saved = logisticsRepository.save(request);
        notifyOrder(saved, "Logistics in transit", "Your delivery is in transit.");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public LogisticsRequestDto markDelivered(Long requestId) {
        LogisticsRequest request = logisticsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        if (!LogisticsStatus.IN_TRANSIT.name().equals(request.getStatus())) {
            throw new IllegalArgumentException("Logistics request must be in transit before delivery confirmation");
        }
        request.setStatus(LogisticsStatus.DELIVERED.name());
        if (Boolean.TRUE.equals(request.getEscrowHeld()) && !Boolean.TRUE.equals(request.getEscrowReleased())) {
            releaseLogisticsEscrow(request);
        }
        LogisticsRequest saved = logisticsRepository.save(request);
        notifyOrder(saved, "Logistics delivered", "Your delivery was marked delivered.");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public LogisticsRequestDto holdEscrow(Long requestId) {
        LogisticsRequest request = logisticsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        if (!Boolean.TRUE.equals(request.getEscrowHeld())) {
            holdLogisticsEscrow(request);
        }
        LogisticsRequest saved = logisticsRepository.save(request);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public LogisticsRequestDto releaseEscrow(Long requestId) {
        LogisticsRequest request = logisticsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        if (Boolean.TRUE.equals(request.getEscrowHeld()) && !Boolean.TRUE.equals(request.getEscrowReleased())) {
            releaseLogisticsEscrow(request);
        }
        LogisticsRequest saved = logisticsRepository.save(request);
        return mapToDto(saved);
    }

    public LogisticsRequestDto mapToDto(LogisticsRequest logisticsRequest) {
        LogisticsRequestDto dto = new LogisticsRequestDto();

        dto.setId(logisticsRequest.getId());
        dto.setPickupLocation(logisticsRequest.getPickupLocation());
        dto.setDeliveryLocation(logisticsRequest.getDeliveryLocation());
        dto.setStatus(logisticsRequest.getStatus());
        dto.setCost(logisticsRequest.getCost());
        dto.setEscrowHeld(logisticsRequest.getEscrowHeld());
        dto.setEscrowReleased(logisticsRequest.getEscrowReleased());
        dto.setAssignedProvider(logisticsRequest.getAssignedProvider());
        dto.setOrder(logisticsRequest.getOrder());

        return dto;
    }

    private void holdLogisticsEscrow(LogisticsRequest request) {
        if (request.getOrder() == null || request.getOrder().getBuyer() == null) {
            throw new ResourceNotFoundException("Logistics request has no buyer order");
        }
        Double amount = request.getCost();
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Logistics cost must be greater than zero");
        }
        Currency currency = request.getOrder().getCurrency() != null ? request.getOrder().getCurrency() : Currency.USD;
        User buyer = request.getOrder().getBuyer();
        subtractBalance(buyer, currency, amount);
        request.setEscrowHeld(true);
        request.setEscrowReleased(false);
        createLogisticsTransaction(request, buyer, amount, currency, TransactionType.ESCROW_HOLD);
        notificationService.sendPaymentUpdate(buyer, "Logistics escrow held",
                "Funds for logistics request " + request.getId() + " have been held in escrow.");
    }

    private void releaseLogisticsEscrow(LogisticsRequest request) {
        if (request.getAssignedProvider() == null) {
            throw new ResourceNotFoundException("Logistics request has no assigned provider");
        }
        Double amount = request.getCost();
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Logistics cost must be greater than zero");
        }
        Currency currency = request.getOrder() != null && request.getOrder().getCurrency() != null
                ? request.getOrder().getCurrency()
                : Currency.USD;
        LogisticsProvider provider = request.getAssignedProvider();
        addBalance(provider, currency, amount);
        request.setEscrowReleased(true);
        createLogisticsTransaction(request, provider, amount, currency, TransactionType.ESCROW_RELEASE);
        notificationService.sendPaymentUpdate(provider, "Logistics escrow released",
                "Funds for logistics request " + request.getId() + " have been released.");
    }

    private void refundLogisticsEscrow(LogisticsRequest request) {
        if (request.getOrder() == null || request.getOrder().getBuyer() == null) {
            throw new ResourceNotFoundException("Logistics request has no buyer order");
        }
        Double amount = request.getCost();
        Currency currency = request.getOrder().getCurrency() != null ? request.getOrder().getCurrency() : Currency.USD;
        User buyer = request.getOrder().getBuyer();
        addBalance(buyer, currency, amount);
        request.setEscrowHeld(false);
        request.setEscrowReleased(false);
        createLogisticsTransaction(request, buyer, amount, currency, TransactionType.REFUND);
        notificationService.sendPaymentUpdate(buyer, "Logistics escrow refunded",
                "Funds for logistics request " + request.getId() + " have been refunded.");
    }

    private void createLogisticsTransaction(LogisticsRequest request, User user, Double amount, Currency currency, TransactionType type) {
        TransactionHistory txn = new TransactionHistory();
        txn.setTransactionDate(LocalDateTime.now());
        txn.setTransactionReference("LOGISTICS-ESCROW-" + System.currentTimeMillis());
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setOrder(request.getOrder());
        txn.setUser(user);
        if (request.getOrder() != null) {
            txn.setBuyer(request.getOrder().getBuyer());
        }
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
                throw new IllegalArgumentException("Insufficient USD balance for logistics escrow");
            }
            user.setUsdBalance(current - amount);
        } else {
            double current = user.getZigBalance() != null ? user.getZigBalance() : 0.0;
            if (current < amount) {
                throw new IllegalArgumentException("Insufficient ZIG balance for logistics escrow");
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

    private void notifyOrder(LogisticsRequest request, String title, String message) {
        if (request.getOrder() == null) return;
        Order order = request.getOrder();
        if (order.getBuyer() != null) {
            notificationService.sendLogisticsUpdate(order.getBuyer(), title, message);
        }
        if (order.getFarmer() != null) {
            notificationService.sendLogisticsUpdate(order.getFarmer(), title, message);
        }
    }
}
