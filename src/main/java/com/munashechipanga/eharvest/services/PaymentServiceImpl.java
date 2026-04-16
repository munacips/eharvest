package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.PaymentRequestDTO;
import com.munashechipanga.eharvest.dtos.response.PaymentResponseDTO;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.services.payments.PaynowClient;
import com.munashechipanga.eharvest.services.payments.PaynowInitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PaynowClient paynowClient;

    @Autowired
    NotificationService notificationService;

    @Override
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO dto) {
        if (dto.getType() == null || dto.getCurrency() == null) {
            throw new IllegalArgumentException("Payment type and currency are required");
        }
        if (dto.getType() != TransactionType.DEPOSIT && dto.getType() != TransactionType.WITHDRAWAL) {
            throw new IllegalArgumentException("Payment type must be DEPOSIT or WITHDRAWAL");
        }
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TransactionHistory txn = new TransactionHistory();
        txn.setTransactionDate(LocalDateTime.now());
        txn.setTransactionReference("TXN-" + System.currentTimeMillis());
        txn.setAmount(dto.getAmount());
        txn.setStatus("INITIATED");
        txn.setType(dto.getType());
        txn.setCurrency(dto.getCurrency());
        txn.setProvider("PAYNOW");
        txn.setUser(user);
        if (user instanceof com.munashechipanga.eharvest.entities.Buyer buyer) {
            txn.setBuyer(buyer);
        }
        if (user instanceof com.munashechipanga.eharvest.entities.Farmer farmer) {
            txn.setFarmer(farmer);
        }

        if (dto.getType() == TransactionType.WITHDRAWAL) {
            subtractBalance(user, dto.getCurrency(), dto.getAmount());
        }

        PaynowInitResponse response = dto.getType() == TransactionType.DEPOSIT
                ? paynowClient.initiateDeposit(dto.getEmail(), dto.getPhoneNumber(), dto.getAmount(),
                dto.getCurrency().name(), txn.getTransactionReference())
                : paynowClient.initiateWithdraw(dto.getEmail(), dto.getPhoneNumber(), dto.getAmount(),
                dto.getCurrency().name(), txn.getTransactionReference());

        txn.setProviderReference(response.getProviderReference());
        transactionRepository.save(txn);

        PaymentResponseDTO out = new PaymentResponseDTO();
        out.setTransactionId(txn.getId());
        out.setTransactionReference(txn.getTransactionReference());
        out.setStatus(txn.getStatus());
        out.setProvider(txn.getProvider());
        out.setProviderReference(txn.getProviderReference());
        out.setRedirectUrl(response.getRedirectUrl());
        out.setCurrency(txn.getCurrency());
        out.setType(txn.getType());
        return out;
    }

    @Override
    public PaymentResponseDTO handleWebhook(String reference, String status, String providerRef) {
        TransactionHistory txn = null;
        if (reference != null) {
            txn = transactionRepository.findByTransactionReference(reference).orElse(null);
        }
        if (txn == null && providerRef != null) {
            txn = transactionRepository.findByProviderReference(providerRef).orElse(null);
        }
        if (txn == null) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        txn.setStatus(status);
        if (providerRef != null) {
            txn.setProviderReference(providerRef);
        }

        User user = resolveUser(txn);
        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            if (txn.getType() == TransactionType.DEPOSIT) {
                addBalance(user, txn.getCurrency(), txn.getAmount());
            }
            notificationService.sendPaymentUpdate(user, "Payment successful",
                    "Transaction " + txn.getTransactionReference() + " completed.");
        } else if ("FAILED".equalsIgnoreCase(status)) {
            if (txn.getType() == TransactionType.WITHDRAWAL) {
                addBalance(user, txn.getCurrency(), txn.getAmount());
            }
            notificationService.sendPaymentUpdate(user, "Payment failed",
                    "Transaction " + txn.getTransactionReference() + " failed.");
        }

        transactionRepository.save(txn);

        PaymentResponseDTO out = new PaymentResponseDTO();
        out.setTransactionId(txn.getId());
        out.setTransactionReference(txn.getTransactionReference());
        out.setStatus(txn.getStatus());
        out.setProvider(txn.getProvider());
        out.setProviderReference(txn.getProviderReference());
        out.setCurrency(txn.getCurrency());
        out.setType(txn.getType());
        return out;
    }

    private User resolveUser(TransactionHistory txn) {
        if (txn.getUser() != null) {
            return txn.getUser();
        }
        if (txn.getBuyer() != null) {
            return txn.getBuyer();
        }
        if (txn.getFarmer() != null) {
            return txn.getFarmer();
        }
        throw new ResourceNotFoundException("Transaction user not found");
    }

    private void subtractBalance(User user, Currency currency, Double amount) {
        if (amount == null) return;
        if (currency == Currency.USD) {
            double current = user.getUsdBalance() != null ? user.getUsdBalance() : 0.0;
            if (current < amount) {
                throw new IllegalArgumentException("Insufficient USD balance");
            }
            user.setUsdBalance(current - amount);
        } else {
            double current = user.getZigBalance() != null ? user.getZigBalance() : 0.0;
            if (current < amount) {
                throw new IllegalArgumentException("Insufficient ZIG balance");
            }
            user.setZigBalance(current - amount);
        }
        userRepository.save(user);
    }

    private void addBalance(User user, Currency currency, Double amount) {
        if (amount == null) return;
        if (currency == Currency.USD) {
            double current = user.getUsdBalance() != null ? user.getUsdBalance() : 0.0;
            user.setUsdBalance(current + amount);
        } else {
            double current = user.getZigBalance() != null ? user.getZigBalance() : 0.0;
            user.setZigBalance(current + amount);
        }
        userRepository.save(user);
    }
}
