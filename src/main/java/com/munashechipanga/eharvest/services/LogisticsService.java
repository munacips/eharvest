package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.entities.LogisticsProvider;

import java.util.List;

public interface LogisticsService {
    LogisticsRequestDto getLogisticsRequestById(Long id);
    LogisticsRequestDto createLogisticsRequest(LogisticsRequestDto dto);
    void deleteLogisticsRequest(Long id);
    LogisticsRequestDto updateLogisticsRequest(Long id, LogisticsRequestDto dto);
    List<LogisticsRequestDto> getAllLogisticsProviders();

}
