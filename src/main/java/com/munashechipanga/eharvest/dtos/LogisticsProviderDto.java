package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogisticsProviderDto extends UserRequestDTO {
    private String licenseNumber;
    private String defensiveId;
}
