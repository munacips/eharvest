package com.munashechipanga.eharvest.dtos.response;

import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.enums.Currency;
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
    private Currency currency;
    private Boolean escrowHeld;
    private Double escrowAmount;
    private Buyer buyer;
    private Farmer farmer;
    private LogisticsRequest logisticsRequest;
    private Boolean escrowReleased;
}
