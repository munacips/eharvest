package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Produce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduceRepository extends JpaRepository<Produce,Long> {
}
