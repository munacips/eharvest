package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogisticsRequestCreateDTO {
    private Long id;
    private String pickupLocation;
    private String deliveryLocation;
    private String status;
    private Double cost;
    private Long assignedProvider;
    private Long order;
}
