package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LogisticsRepository extends JpaRepository<LogisticsRequest,Long>, JpaSpecificationExecutor<LogisticsRequest> {
    Optional<LogisticsRequest> findByOrder_Id(Long orderId);
}
