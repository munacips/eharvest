package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentRepository extends JpaRepository<TransactionHistory,Long>, JpaSpecificationExecutor<TransactionHistory> {
}
