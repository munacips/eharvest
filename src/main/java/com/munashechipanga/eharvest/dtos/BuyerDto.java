package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerDto extends UserRequestDTO {
    private String companyName;
    private Integer successfulBuys;
    private Integer unsuccessfulBuys;

}
