package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.LogisticsProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleDto {
    private Long id;
    private String plateNumber;
    private String type;
    private String colour;
    private LogisticsProvider owner;
}
