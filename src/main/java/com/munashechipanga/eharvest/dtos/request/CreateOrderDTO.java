package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateOrderDTO {
    private Long id;
    private Double totalAmount;
    private String status;
    private Long buyerId;
    private Long farmerId;
    private Long logisticsRequestId;
    private Boolean escrowReleased;
}
