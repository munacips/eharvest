package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByBuyer_Id(Long buyerId);
    List<Subscription> findByFarmer_Id(Long farmerId);
    List<Subscription> findByStatusAndNextDeliveryDateBefore(String status, LocalDateTime dateTime);
}
