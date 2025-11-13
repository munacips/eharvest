package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<Buyer,Long> {
}
