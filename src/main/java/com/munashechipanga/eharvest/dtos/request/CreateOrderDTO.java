package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.munashechipanga.eharvest.enums.Currency;

@Getter
@Setter
public class CreateOrderDTO {
    private Long id;
    private Double totalAmount;
    private String status;
    private Currency currency;
    private Double escrowAmount;
    private Long buyerId;
    private Long farmerId;
    private Long logisticsRequestId;
    private Boolean escrowReleased;
}
