package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BuyerRepository extends JpaRepository<Buyer,Long>, JpaSpecificationExecutor<Buyer> {
}
