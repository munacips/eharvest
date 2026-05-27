package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.entities.Order;

import java.util.List;

public interface ReviewService {

    ReviewDto createReview(ReviewDto dto);
    ReviewDto updateReview(Long id,ReviewDto dto);
    ReviewDto getReviewById(Long id);
    void deleteReview(Long id);
    List<ReviewDto> getAllReviews();
    List<ReviewDto> getReviewsByReviewerId(Long reviewerId);
    List<ReviewDto> getReviewsByRevieweeId(Long revieweeId);
    List<ReviewDto> getReviewsByOrderId(Long orderId);
    List<ReviewDto> getPendingReviewsByReviewerId(Long reviewerId);
    void createPendingReviewsForOrder(Order order);
}
