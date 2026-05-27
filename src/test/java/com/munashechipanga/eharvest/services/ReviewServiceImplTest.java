package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.enums.LogisticsType;
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.enums.ReviewStatus;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.ReviewRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void createPendingReviewsForOrderCreatesBuyerFarmerAndProviderPairs() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        LogisticsProvider provider = provider(3L);
        Order order = deliveredOrder(10L, buyer, farmer, provider);

        when(reviewRepository.existsByOrder_IdAndReviewer_IdAndReviewee_Id(any(), any(), any())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.createPendingReviewsForOrder(order);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, times(6)).save(captor.capture());
        List<Review> createdReviews = captor.getAllValues();

        assertThat(createdReviews)
                .hasSize(6)
                .allSatisfy(review -> {
                    assertThat(review.getOrder()).isEqualTo(order);
                    assertThat(review.getStatus()).isEqualTo(ReviewStatus.PENDING);
                    assertThat(review.getRating()).isNull();
                    assertThat(review.getComment()).isNull();
                });
    }

    @Test
    void getPendingReviewsByReviewerReturnsOnlyPendingReviews() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        Order order = deliveredOrder(10L, buyer, farmer, null);

        Review pending = pendingReview(100L, order, buyer, farmer);
        Review completed = completedReview(101L, order, buyer, farmer, 4);

        when(reviewRepository.findByReviewer_IdAndStatus(buyer.getId(), ReviewStatus.PENDING))
                .thenReturn(List.of(pending));

        List<ReviewDto> result = reviewService.getPendingReviewsByReviewerId(buyer.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(pending.getId());
        assertThat(result.getFirst().getStatus()).isEqualTo(ReviewStatus.PENDING);
        assertThat(result).extracting(ReviewDto::getId).doesNotContain(completed.getId());
    }

    @Test
    void updateReviewCompletesPendingReviewAndUpdatesTrustScore() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        Order order = deliveredOrder(10L, buyer, farmer, null);
        Review pending = pendingReview(100L, order, buyer, farmer);

        ReviewDto dto = new ReviewDto();
        dto.setOrderId(order.getId());
        dto.setReviewerId(buyer.getId());
        dto.setRevieweeId(farmer.getId());
        dto.setRating(5);
        dto.setComment("Excellent service");
        dto.setStatus(ReviewStatus.COMPLETED);

        when(reviewRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.findAverageRatingForUser(farmer.getId())).thenReturn(4.7);

        ReviewDto updated = reviewService.updateReview(pending.getId(), dto);

        assertThat(updated.getStatus()).isEqualTo(ReviewStatus.COMPLETED);
        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getComment()).isEqualTo("Excellent service");
        assertThat(farmer.getTrustScore()).isEqualTo(5);
        verify(userRepository).save(farmer);
    }

    @Test
    void createPendingReviewsForOrderSkipsDuplicatePairs() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        Order order = deliveredOrder(10L, buyer, farmer, null);

        when(reviewRepository.existsByOrder_IdAndReviewer_IdAndReviewee_Id(order.getId(), buyer.getId(), farmer.getId()))
                .thenReturn(true);
        when(reviewRepository.existsByOrder_IdAndReviewer_IdAndReviewee_Id(order.getId(), farmer.getId(), buyer.getId()))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.createPendingReviewsForOrder(order);

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createPendingReviewDoesNotUpdateTrustScore() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        Order order = deliveredOrder(10L, buyer, farmer, null);
        ReviewDto dto = new ReviewDto();
        dto.setOrderId(order.getId());
        dto.setReviewerId(buyer.getId());
        dto.setRevieweeId(farmer.getId());

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        when(userRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));
        when(reviewRepository.existsByOrder_IdAndReviewer_IdAndReviewee_Id(order.getId(), buyer.getId(), farmer.getId()))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDto created = reviewService.createReview(dto);

        assertThat(created.getStatus()).isEqualTo(ReviewStatus.PENDING);
        verify(userRepository, never()).save(any());
        verify(reviewRepository, never()).findAverageRatingForUser(anyLong());
    }

    @Test
    void createReviewRejectsIfOrderIsNotDelivered() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        Order order = order(10L, buyer, farmer, null, OrderStatus.ACCEPTED.name());
        ReviewDto dto = new ReviewDto();
        dto.setOrderId(order.getId());
        dto.setReviewerId(buyer.getId());
        dto.setRevieweeId(farmer.getId());

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        when(userRepository.findById(farmer.getId())).thenReturn(Optional.of(farmer));

        assertThatThrownBy(() -> reviewService.createReview(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reviews are only allowed after delivery");
    }

    @Test
    void updateReviewRejectsOrderReviewerOrRevieweeChanges() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        LogisticsProvider provider = provider(3L);
        Order order = deliveredOrder(10L, buyer, farmer, provider);
        Review pending = pendingReview(100L, order, buyer, farmer);

        ReviewDto dto = new ReviewDto();
        dto.setOrderId(999L);
        dto.setReviewerId(provider.getId());
        dto.setRevieweeId(farmer.getId());
        dto.setRating(5);

        when(reviewRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> reviewService.updateReview(pending.getId(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order cannot be changed when completing a review");
    }

    @Test
    void deleteReviewUpdatesTrustScoreUsingCompletedReviewsOnly() {
        Farmer farmer = farmer(2L);
        Review review = new Review();
        review.setId(10L);
        review.setReviewee(farmer);
        review.setStatus(ReviewStatus.COMPLETED);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.findAverageRatingForUser(farmer.getId())).thenReturn(null);

        reviewService.deleteReview(review.getId());

        assertThat(farmer.getTrustScore()).isZero();
        verify(reviewRepository).delete(review);
        verify(userRepository).save(farmer);
    }

    @Test
    void deleteReviewReturnsNotFoundForMissingReview() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Review not found");
    }

    private Review pendingReview(Long id, Order order, Buyer buyer, Farmer farmer) {
        Review review = new Review();
        review.setId(id);
        review.setOrder(order);
        review.setReviewer(buyer);
        review.setReviewee(farmer);
        review.setStatus(ReviewStatus.PENDING);
        return review;
    }

    private Review completedReview(Long id, Order order, Buyer buyer, Farmer farmer, int rating) {
        Review review = pendingReview(id, order, buyer, farmer);
        review.setStatus(ReviewStatus.COMPLETED);
        review.setRating(rating);
        review.setComment("Completed");
        return review;
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

    private LogisticsProvider provider(Long id) {
        LogisticsProvider provider = new LogisticsProvider();
        provider.setId(id);
        provider.setRole("LOGISTICS");
        return provider;
    }

    private Order deliveredOrder(Long id, Buyer buyer, Farmer farmer, LogisticsProvider provider) {
        return order(id, buyer, farmer, provider, OrderStatus.DELIVERED.name());
    }

    private Order order(Long id, Buyer buyer, Farmer farmer, LogisticsProvider provider, String status) {
        Order order = new Order();
        order.setId(id);
        order.setBuyer(buyer);
        order.setFarmer(farmer);
        order.setStatus(status);
        order.setLogisticsType(provider == null ? LogisticsType.BUYER_PICKUP : LogisticsType.THIRD_PARTY);

        if (provider != null) {
            LogisticsRequest logisticsRequest = new LogisticsRequest();
            logisticsRequest.setId(50L);
            logisticsRequest.setAssignedProvider(provider);
            order.setLogisticsRequest(logisticsRequest);
        }
        return order;
    }
}
