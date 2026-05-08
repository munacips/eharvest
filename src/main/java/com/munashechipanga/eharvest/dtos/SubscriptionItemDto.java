package com.munashechipanga.eharvest.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionItemDto {
    private Long id;
    private Long produceId;
    private Double quantity;
    private Double unitPrice;
}
