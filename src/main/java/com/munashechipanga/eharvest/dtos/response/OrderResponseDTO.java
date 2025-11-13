package com.munashechipanga.eharvest.dtos.response;

import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderResponseDTO {
    private Long id;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private Buyer buyer;
    private LogisticsRequest logisticsRequest;
    private Boolean escrowReleased;
}
