package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequestDTO {
    private Long userId;
    private String channel; // EMAIL or SMS
}
