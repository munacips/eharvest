package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionHistory,Long>, JpaSpecificationExecutor<TransactionHistory> {
    Optional<TransactionHistory> findByTransactionReference(String reference);
    Optional<TransactionHistory> findByProviderReference(String providerReference);
}
