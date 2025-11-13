package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.LogisticsProviderDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;

import java.util.List;

public interface LogisticsProviderService {
    UserResponseDTO getLogisticsProviderById(Long id);
    UserResponseDTO createLogisticsProvider(LogisticsProviderDto dto);
    UserResponseDTO updateLogisticsProvider(Long id,LogisticsProviderDto dto);
    void deleteLogisticsProvider(Long id);
    List<UserResponseDTO> getAllLogisticsProviders();
}
