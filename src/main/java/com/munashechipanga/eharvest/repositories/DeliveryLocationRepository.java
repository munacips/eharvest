package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.DeliveryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryLocationRepository extends JpaRepository<DeliveryLocation, Long> {
    Optional<DeliveryLocation> findByOrder_Id(Long orderId);
}
