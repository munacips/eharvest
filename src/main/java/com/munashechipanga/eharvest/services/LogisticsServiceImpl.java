package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.dtos.request.LogisticsRequestCreateDTO;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.LogisticsProviderRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        request.setStatus("SEARCHING");
        request.setCost(dto.getCost());


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

    public LogisticsRequestDto mapToDto(LogisticsRequest logisticsRequest) {
        LogisticsRequestDto dto = new LogisticsRequestDto();

        dto.setId(logisticsRequest.getId());
        dto.setPickupLocation(logisticsRequest.getPickupLocation());
        dto.setDeliveryLocation(logisticsRequest.getDeliveryLocation());
        dto.setStatus(logisticsRequest.getStatus());
        dto.setCost(logisticsRequest.getCost());
        dto.setAssignedProvider(logisticsRequest.getAssignedProvider());
        dto.setOrder(logisticsRequest.getOrder());

        return dto;
    }
}
