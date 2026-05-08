package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.LogisticsType;

@Getter
@Setter
public class CreateOrderDTO {
    private Long id;
    private Double totalAmount;
    private String status;
    private Currency currency;
    private LogisticsType logisticsType;
    private Double escrowAmount;
    private Long buyerId;
    private Long farmerId;
    private Long logisticsRequestId;
    private Boolean escrowReleased;
}
