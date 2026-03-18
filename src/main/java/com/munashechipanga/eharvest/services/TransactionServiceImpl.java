package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.TransactionDto;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService{


    @Autowired
    private TransactionRepository repository;

    @Override
    public TransactionDto createTransaction(TransactionDto dto) {
        TransactionHistory transaction = new TransactionHistory();

        //set to current datetime
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setFarmer(dto.getFarmer());
        transaction.setBuyer(dto.getBuyer());
        transaction.setAmount(dto.getAmount());
        transaction.setStatus("INITIATED");
        transaction.setOrder(dto.getOrder());

        TransactionHistory txn =  repository.save(transaction);

        return mapToResponse(txn);
    }

    @Override
    public void deleteTransaction(Long id) {
        TransactionHistory txn = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Transaction not found"));
        repository.delete(txn);
    }

    @Override
    public TransactionDto updateTransaction(Long id, TransactionDto dto) {
        TransactionHistory txn = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Transaction with ID: " + id + " not found"));
        if(dto.getStatus() != null) txn.setStatus(dto.getStatus());
        if(dto.getAmount() != null) txn.setAmount(dto.getAmount());
        if(dto.getOrder() != null) txn.setOrder(dto.getOrder());
        if(dto.getBuyer() != null) txn.setBuyer(dto.getBuyer());
        if(dto.getFarmer() != null) txn.setFarmer(dto.getFarmer());
        if(dto.getTransactionDate() != null) txn.setTransactionDate(dto.getTransactionDate());
        if(dto.getTransactionReference() != null) txn.setTransactionReference(dto.getTransactionReference());

        TransactionHistory updatedTxn = repository.save(txn);
        return mapToResponse(updatedTxn);
    }

    @Override
    public TransactionDto getTransactionById(Long id) {
        TransactionHistory txn = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Transaction with ID: " + id + " not found!"));
        return mapToResponse(txn);
    }

    @Override
    public List<TransactionDto> getAllTransactions() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateTransactionReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int randomNum = new Random().nextInt(9000) + 1000;
        return "TXN-" + timestamp + "-" + randomNum;
    }

    private TransactionDto mapToResponse(TransactionHistory txn){
        TransactionDto dto = new TransactionDto();
        dto.setTransactionReference(txn.getTransactionReference());
        dto.setFarmer(txn.getFarmer());
        dto.setBuyer(txn.getBuyer());
        dto.setOrder(txn.getOrder());
        dto.setTransactionDate(txn.getTransactionDate());
        dto.setId(txn.getId());
        dto.setAmount(txn.getAmount());
        dto.setStatus(txn.getStatus());
        return dto;
    }

}
