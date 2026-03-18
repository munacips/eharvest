package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto {
    private Long id;
    private LocalDateTime transactionDate;
    private Double amount;
    private String status;
    private String transactionReference;
    private Buyer buyer;
    private Farmer farmer;
    private Order order;
}
