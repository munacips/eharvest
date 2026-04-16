package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionFilter {
    private String status;
    private TransactionType type;
    private Currency currency;
}
