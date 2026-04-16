package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.ReviewRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    ReviewRepository repository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public ReviewDto createReview(ReviewDto dto) {
        validateReview(dto);
        Review review = new Review();

        review.setRating(dto.getRating());
        review.setReviewer(dto.getReviewer());
        review.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.LocalDateTime.now());
        review.setReviewee(dto.getReviewee());
        review.setComment(dto.getComment());

        Review savedReview = repository.save(review);
        updateTrustScore(savedReview.getReviewee());
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
        updateTrustScore(savedReview.getReviewee());
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

    @Override
    public List<ReviewDto> getReviewsByReviewerId(Long reviewerId) {
        return repository.findByReviewer_Id(reviewerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByRevieweeId(Long revieweeId) {
        return repository.findByReviewee_Id(revieweeId).stream()
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

    private void validateReview(ReviewDto dto) {
        if (dto.getReviewer() == null || dto.getReviewee() == null) {
            throw new IllegalArgumentException("Reviewer and reviewee are required");
        }

        User reviewer = dto.getReviewer();
        User reviewee = dto.getReviewee();

        boolean canReview = false;
        if ((reviewer instanceof Buyer || reviewer instanceof Farmer) &&
                (reviewee instanceof Buyer || reviewee instanceof Farmer)) {
            canReview = orderRepository.existsDeliveredBetweenBuyerFarmer(
                    reviewer.getId(), reviewee.getId(), OrderStatus.DELIVERED.name());
        } else if (reviewee instanceof LogisticsProvider) {
            canReview = orderRepository.existsDeliveredWithProvider(
                    reviewer.getId(), reviewee.getId(), OrderStatus.DELIVERED.name());
        } else if (reviewer instanceof LogisticsProvider) {
            canReview = orderRepository.existsDeliveredWithProvider(
                    reviewee.getId(), reviewer.getId(), OrderStatus.DELIVERED.name());
        }

        if (!canReview) {
            throw new IllegalArgumentException("No completed delivery between reviewer and reviewee");
        }
    }

    private void updateTrustScore(User user) {
        if (user == null) return;
        Double avg = repository.findAverageRatingForUser(user.getId());
        if (avg == null) return;
        user.setTrustScore((int) Math.round(avg));
        userRepository.save(user);
    }
}
