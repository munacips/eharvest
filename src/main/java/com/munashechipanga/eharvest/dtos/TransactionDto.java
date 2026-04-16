package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.TransactionType;
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
    private TransactionType type;
    private Currency currency;
    private String provider;
    private String providerReference;
    private User user;
    private Buyer buyer;
    private Farmer farmer;
    private Order order;
}
