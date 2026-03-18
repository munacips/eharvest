package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.Vehicle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleImageDto {
    private Long id;
    private String imageUrl;
    private Vehicle vehicle;
}
