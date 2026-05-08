package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.enums.Currency;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SubscriptionDto {
    private Long id;
    private Long buyerId;
    private Long farmerId;
    private String frequency;
    private String status;
    private Boolean requiresLogistics;
    private String pickupAddress;
    private Currency currency;
    private LocalDateTime startDate;
    private List<SubscriptionItemDto> items;
}
