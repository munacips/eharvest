package com.munashechipanga.eharvest.dtos.request;

import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDTO {
    private Long userId;
    private Double amount;
    private Currency currency;
    private TransactionType type; // DEPOSIT or WITHDRAWAL
    private String email;
    private String phoneNumber;
}
