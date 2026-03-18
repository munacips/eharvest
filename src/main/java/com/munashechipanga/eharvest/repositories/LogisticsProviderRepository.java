package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.LogisticsProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogisticsProviderRepository extends JpaRepository<LogisticsProvider,Long>, JpaSpecificationExecutor<LogisticsProvider> {
}
