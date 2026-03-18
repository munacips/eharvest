package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Produce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProduceRepository extends JpaRepository<Produce,Long>, JpaSpecificationExecutor<Produce> {
}
