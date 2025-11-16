package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    ReviewRepository repository;

    @Override
    public ReviewDto createReview(ReviewDto dto) {
        Review review = new Review();

        review.setRating(dto.getRating());
        review.setReviewer(dto.getReviewer());
        review.setCreatedAt(dto.getCreatedAt());
        review.setReviewee(dto.getReviewee());
        review.setComment(dto.getComment());

        Review savedReview = repository.save(review);
        return mapToDto(savedReview);
    }

    @Override
    public ReviewDto updateReview(Long id, ReviewDto dto) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if(dto.getReviewer() != null) review.setReviewer(dto.getReviewer());
        if(dto.getComment() != null) review.setComment(dto.getComment());
        if(dto.getReviewee() != null) review.setReviewee(dto.getReviewee());
        if(dto.getCreatedAt() != null) review.setCreatedAt(dto.getCreatedAt());
        if(dto.getRating() != null) review.setRating(dto.getRating());

        Review savedReview = repository.save(review);
        return mapToDto(savedReview);
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        return mapToDto(review);
    }

    @Override
    public void deleteReview(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();

        dto.setId(review.getId());
        dto.setReviewer(review.getReviewer());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setReviewee(review.getReviewee());
        dto.setCreatedAt(review.getCreatedAt());

        return dto;
    }
}
