package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.ReviewRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReviewServiceImpl reviewService;

    @Test
    void createReviewLoadsUsersByIdAndUpdatesRevieweeTrustScore() {
        Buyer reviewer = buyer(1L);
        Farmer reviewee = farmer(2L);

        ReviewDto dto = new ReviewDto();
        dto.setReviewerId(reviewer.getId());
        dto.setRevieweeId(reviewee.getId());
        dto.setRating(5);
        dto.setComment("Great produce");

        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(reviewee.getId())).thenReturn(Optional.of(reviewee));
        when(orderRepository.existsDeliveredBetweenBuyerFarmer(
                reviewer.getId(), reviewee.getId(), OrderStatus.DELIVERED.name()))
                .thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(10L);
            return review;
        });
        when(reviewRepository.findAverageRatingForUser(reviewee.getId())).thenReturn(4.6);

        ReviewDto created = reviewService.createReview(dto);

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getReviewerId()).isEqualTo(reviewer.getId());
        assertThat(created.getRevieweeId()).isEqualTo(reviewee.getId());
        assertThat(created.getReviewee().getRole()).isEqualTo("FARMER");
        assertThat(reviewee.getTrustScore()).isEqualTo(5);
        verify(userRepository).save(reviewee);
    }

    @Test
    void createReviewRejectsInvalidRatingBeforeSaving() {
        Buyer reviewer = buyer(1L);
        Farmer reviewee = farmer(2L);

        ReviewDto dto = new ReviewDto();
        dto.setReviewerId(reviewer.getId());
        dto.setRevieweeId(reviewee.getId());
        dto.setRating(6);

        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(reviewee.getId())).thenReturn(Optional.of(reviewee));

        assertThatThrownBy(() -> reviewService.createReview(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rating must be between 1 and 5");
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReviewUpdatesRevieweeTrustScoreToZeroWhenNoReviewsRemain() {
        Farmer reviewee = farmer(2L);
        Review review = new Review();
        review.setId(10L);
        review.setReviewee(reviewee);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.findAverageRatingForUser(reviewee.getId())).thenReturn(null);

        reviewService.deleteReview(review.getId());

        assertThat(reviewee.getTrustScore()).isZero();
        verify(reviewRepository).delete(review);
        verify(userRepository).save(reviewee);
    }

    @Test
    void deleteReviewReturnsNotFoundForMissingReview() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Review not found");
    }

    private Buyer buyer(Long id) {
        Buyer buyer = new Buyer();
        buyer.setId(id);
        buyer.setRole("BUYER");
        return buyer;
    }

    private Farmer farmer(Long id) {
        Farmer farmer = new Farmer();
        farmer.setId(id);
        farmer.setRole("FARMER");
        return farmer;
    }
}
