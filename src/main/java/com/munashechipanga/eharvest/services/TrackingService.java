package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LocationPayloadDTO;
import com.munashechipanga.eharvest.dtos.request.LocationUpdateDTO;
import com.munashechipanga.eharvest.entities.DeliveryLocation;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.enums.LogisticsStatus;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.DeliveryLocationRepository;
import com.munashechipanga.eharvest.repositories.LogisticsProviderRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final DeliveryLocationRepository locationRepository;
    private final OrderRepository orderRepository;
    private final LogisticsProviderRepository providerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public LocationPayloadDTO updateLocation(LocationUpdateDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        LogisticsRequest logisticsRequest = order.getLogisticsRequest();
        if (logisticsRequest == null || !LogisticsStatus.IN_TRANSIT.name().equals(logisticsRequest.getStatus())) {
            throw new IllegalStateException("Order is not currently in transit");
        }

        LogisticsProvider provider = providerRepository.findById(dto.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        if (logisticsRequest.getAssignedProvider() == null) {
            throw new IllegalStateException("Order does not have an assigned logistics provider");
        }
        if (!logisticsRequest.getAssignedProvider().getId().equals(provider.getId())) {
            throw new IllegalArgumentException("Provider is not assigned to this order");
        }

        DeliveryLocation location = locationRepository.findByOrder_Id(dto.getOrderId())
                .orElseGet(DeliveryLocation::new);

        location.setOrder(order);
        location.setProvider(provider);
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setHeading(dto.getHeading());
        location.setSpeed(dto.getSpeed());
        DeliveryLocation savedLocation = locationRepository.save(location);

        LocationPayloadDTO payload = new LocationPayloadDTO(
                order.getId(),
                provider.getId(),
                savedLocation.getLatitude(),
                savedLocation.getLongitude(),
                savedLocation.getHeading(),
                savedLocation.getSpeed(),
                savedLocation.getUpdatedAt() != null ? savedLocation.getUpdatedAt() : LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/tracking/" + order.getId(), payload);
        log.debug("Location broadcast for order {}: lat={}, lng={}",
                order.getId(), payload.getLatitude(), payload.getLongitude());

        return payload;
    }

    @Transactional(readOnly = true)
    public LocationPayloadDTO getLastKnownLocation(Long orderId) {
        DeliveryLocation location = locationRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No location data for order " + orderId));

        return new LocationPayloadDTO(
                orderId,
                location.getProvider().getId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getHeading(),
                location.getSpeed(),
                location.getUpdatedAt()
        );
    }
}
