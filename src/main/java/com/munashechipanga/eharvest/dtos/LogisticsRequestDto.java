package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogisticsRequestDto {
    private Long id;
    private String pickupLocation;
    private String deliveryLocation;
    private String status;
    private Double cost;
    private LogisticsProvider assignedProvider;
    private Order order;
}
