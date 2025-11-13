package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
}
