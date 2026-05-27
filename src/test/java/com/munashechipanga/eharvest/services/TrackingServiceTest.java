package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LocationPayloadDTO;
import com.munashechipanga.eharvest.dtos.request.LocationUpdateDTO;
import com.munashechipanga.eharvest.entities.DeliveryLocation;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.enums.LogisticsStatus;
import com.munashechipanga.eharvest.repositories.DeliveryLocationRepository;
import com.munashechipanga.eharvest.repositories.LogisticsProviderRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    DeliveryLocationRepository locationRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    LogisticsProviderRepository providerRepository;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    TrackingService trackingService;

    @Test
    void updateLocationBroadcastsPayloadForAssignedProvider() {
        LogisticsProvider provider = new LogisticsProvider();
        provider.setId(7L);

        LogisticsRequest logisticsRequest = new LogisticsRequest();
        logisticsRequest.setStatus(LogisticsStatus.IN_TRANSIT.name());
        logisticsRequest.setAssignedProvider(provider);

        Order order = new Order();
        order.setId(11L);
        order.setLogisticsRequest(logisticsRequest);

        LocationUpdateDTO dto = new LocationUpdateDTO();
        dto.setOrderId(order.getId());
        dto.setProviderId(provider.getId());
        dto.setLatitude(-17.8249);
        dto.setLongitude(31.0530);
        dto.setHeading(90.0);
        dto.setSpeed(42.0);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(providerRepository.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(locationRepository.findByOrder_Id(order.getId())).thenReturn(Optional.empty());
        when(locationRepository.save(any(DeliveryLocation.class))).thenAnswer(invocation -> {
            DeliveryLocation saved = invocation.getArgument(0);
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        LocationPayloadDTO payload = trackingService.updateLocation(dto);

        assertThat(payload.getOrderId()).isEqualTo(order.getId());
        assertThat(payload.getProviderId()).isEqualTo(provider.getId());
        assertThat(payload.getLatitude()).isEqualTo(dto.getLatitude());
        verify(messagingTemplate).convertAndSend("/topic/tracking/" + order.getId(), payload);
    }

    @Test
    void updateLocationRejectsProviderNotAssignedToOrder() {
        LogisticsProvider assignedProvider = new LogisticsProvider();
        assignedProvider.setId(7L);

        LogisticsProvider otherProvider = new LogisticsProvider();
        otherProvider.setId(8L);

        LogisticsRequest logisticsRequest = new LogisticsRequest();
        logisticsRequest.setStatus(LogisticsStatus.IN_TRANSIT.name());
        logisticsRequest.setAssignedProvider(assignedProvider);

        Order order = new Order();
        order.setId(11L);
        order.setLogisticsRequest(logisticsRequest);

        LocationUpdateDTO dto = new LocationUpdateDTO();
        dto.setOrderId(order.getId());
        dto.setProviderId(otherProvider.getId());

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(providerRepository.findById(otherProvider.getId())).thenReturn(Optional.of(otherProvider));

        assertThatThrownBy(() -> trackingService.updateLocation(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provider is not assigned to this order");
    }
}
