package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.LogisticsProvider;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class VehicleDto {
    private Long id;
    private String plateNumber;
    private String type;
    private String colour;
    private LogisticsProvider owner;
    private List<String> imageUrls = new ArrayList<>();
}
