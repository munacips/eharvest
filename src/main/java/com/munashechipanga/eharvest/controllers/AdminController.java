package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.TransactionDto;
import com.munashechipanga.eharvest.dtos.TransactionFilter;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.services.UserService;
import com.munashechipanga.eharvest.specs.TransactionSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}/active")
    public ResponseEntity<?> setActive(@PathVariable Long id, @RequestParam boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(active);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/verified")
    public ResponseEntity<?> setVerified(@PathVariable Long id, @RequestParam boolean verified) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerified(verified);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) com.munashechipanga.eharvest.enums.TransactionType type,
            @RequestParam(required = false) com.munashechipanga.eharvest.enums.Currency currency
    ) {
        TransactionFilter filter = new TransactionFilter();
        filter.setStatus(status);
        filter.setType(type);
        filter.setCurrency(currency);
        List<TransactionHistory> txns = transactionRepository.findAll(TransactionSpecifications.withFilters(filter));
        return ResponseEntity.ok(txns.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    private TransactionDto mapToDto(TransactionHistory txn) {
        TransactionDto dto = new TransactionDto();
        dto.setId(txn.getId());
        dto.setTransactionReference(txn.getTransactionReference());
        dto.setTransactionDate(txn.getTransactionDate());
        dto.setAmount(txn.getAmount());
        dto.setStatus(txn.getStatus());
        dto.setBuyer(txn.getBuyer());
        dto.setFarmer(txn.getFarmer());
        dto.setOrder(txn.getOrder());
        dto.setUser(txn.getUser());
        dto.setCurrency(txn.getCurrency());
        dto.setType(txn.getType());
        dto.setProvider(txn.getProvider());
        dto.setProviderReference(txn.getProviderReference());
        return dto;
    }
}
