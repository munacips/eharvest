package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long>, JpaSpecificationExecutor<Review> {
    List<Review> findByReviewer_Id(Long reviewerId);
    List<Review> findByReviewee_Id(Long revieweeId);
}
