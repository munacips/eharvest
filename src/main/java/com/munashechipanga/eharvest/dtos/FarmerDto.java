package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FarmerDto extends UserRequestDTO {
    private String farmName;
    private String FarmLocation;
    private Integer successfulSales;
    private Integer unsuccessfulSales;
}
