package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.LogisticsRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogisticsRepository extends JpaRepository<LogisticsRequest,Long> {
}
