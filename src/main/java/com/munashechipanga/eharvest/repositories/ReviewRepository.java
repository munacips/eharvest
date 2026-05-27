package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long>, JpaSpecificationExecutor<Review> {
    List<Review> findByReviewer_Id(Long reviewerId);
    List<Review> findByReviewer_IdAndStatus(Long reviewerId, ReviewStatus status);
    List<Review> findByReviewee_Id(Long revieweeId);
    List<Review> findByOrder_Id(Long orderId);

    boolean existsByOrder_IdAndReviewer_IdAndReviewee_Id(Long orderId, Long reviewerId, Long revieweeId);

    @Query("""
            select avg(r.rating)
            from Review r
            where r.reviewee.id = :userId
              and r.status = com.munashechipanga.eharvest.enums.ReviewStatus.COMPLETED
            """)
    Double findAverageRatingForUser(@Param("userId") Long userId);
}
