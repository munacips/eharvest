package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;

import java.util.List;

public interface ReviewService {

    ReviewDto createReview(ReviewDto dto);
    ReviewDto updateReview(Long id,ReviewDto dto);
    ReviewDto getReviewById(Long id);
    void deleteReview(Long id);
    List<ReviewDto> getAllReviews();
}
