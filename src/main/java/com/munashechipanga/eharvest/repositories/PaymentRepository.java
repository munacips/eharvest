package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<TransactionHistory,Long> {
}
