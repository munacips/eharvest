package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
}
