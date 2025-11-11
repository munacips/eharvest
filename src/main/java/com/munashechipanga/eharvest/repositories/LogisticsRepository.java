package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.LogisticsProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogisticsRepository extends JpaRepository<LogisticsProvider,Long> {
}
