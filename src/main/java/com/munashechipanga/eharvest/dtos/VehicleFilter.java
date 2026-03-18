package com.munashechipanga.eharvest.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleFilter {
    private String type;
    private String colour;
    private String plateNumber; // exact or like via search
    private Long ownerId;
    private String search; // matches plateNumber/type/colour

}
