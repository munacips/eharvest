package com.munashechipanga.eharvest.market;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long> {
    boolean existsByCity(String city);
}
