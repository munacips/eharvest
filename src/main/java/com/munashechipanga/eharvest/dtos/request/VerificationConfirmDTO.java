package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationConfirmDTO {
    private Long userId;
    private String code;
}
