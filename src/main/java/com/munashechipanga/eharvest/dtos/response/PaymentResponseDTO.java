package com.munashechipanga.eharvest.dtos.response;

import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDTO {
    private Long transactionId;
    private String transactionReference;
    private String status;
    private String provider;
    private String providerReference;
    private String redirectUrl;
    private Currency currency;
    private TransactionType type;
}
