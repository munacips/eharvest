package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    LogisticsRepository logisticsRepository;

    @Override
    public LogisticsRequestDto getLogisticsRequestById(Long id) {
        LogisticsRequest request = logisticsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));
        return mapToDto(request);
    }

    @Override
    public LogisticsRequestDto createLogisticsRequest(LogisticsRequestDto dto) {

        LogisticsRequest request = new LogisticsRequest();
        request.setPickupLocation( dto.getPickupLocation());
        request.setDeliveryLocation(dto.getDeliveryLocation());
        request.setStatus("AWAITING_PICKUP");
        request.setCost( dto.getCost());
        request.setAssignedProvider( dto.getAssignedProvider());
        request.setOrder( dto.getOrder());

        LogisticsRequest newRequest = logisticsRepository.save(request);

        return mapToDto(newRequest);
    }

    @Override
    public void deleteLogisticsRequest(Long id) {
        logisticsRepository.deleteById(id);
    }

    @Override
    public LogisticsRequestDto updateLogisticsRequest(Long id, LogisticsRequestDto dto) {
        LogisticsRequest request = logisticsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logistics request not found"));

        if(dto.getPickupLocation() != null) request.setPickupLocation(dto.getPickupLocation());
        if(dto.getDeliveryLocation() != null) request.setDeliveryLocation(dto.getDeliveryLocation());
        if(dto.getStatus() != null)  request.setStatus(dto.getStatus());
        if(dto.getCost() != null)  request.setCost(dto.getCost());
        if(dto.getAssignedProvider() != null)  request.setAssignedProvider(dto.getAssignedProvider());
        if(dto.getOrder() != null)  request.setOrder(dto.getOrder());

        LogisticsRequest newRequest = logisticsRepository.save(request);

        return mapToDto(newRequest);
    }

    @Override
    public List<LogisticsRequestDto> getAllLogisticsProviders() {
        return logisticsRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
