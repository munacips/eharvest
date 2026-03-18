package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.dtos.request.LogisticsRequestCreateDTO;

import java.util.List;

public interface LogisticsService {
    LogisticsRequestDto getLogisticsRequestById(Long id);
    LogisticsRequestDto createLogisticsRequest(LogisticsRequestCreateDTO dto);
    void deleteLogisticsRequest(Long id);
    LogisticsRequestDto updateLogisticsRequest(Long id, LogisticsRequestCreateDTO dto);
    List<LogisticsRequestDto> getAllLogisticsProviders();
    LogisticsRequestDto getLogisticsRequestByOrderId(Long id);

}
