package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.TransactionDto;

import java.util.List;

public interface TransactionService {
    TransactionDto createTransaction(TransactionDto dto);
    void deleteTransaction(Long id);
    TransactionDto updateTransaction(Long id, TransactionDto dto);
    TransactionDto getTransactionById(Long id);
    List<TransactionDto> getAllTransactions();
}
