package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Produce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProduceRepository extends JpaRepository<Produce,Long>, JpaSpecificationExecutor<Produce> {
    List<Produce> findByFarmer_Id(Long farmerId);
}
