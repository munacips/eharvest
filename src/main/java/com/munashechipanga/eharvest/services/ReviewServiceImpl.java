package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.Review;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.enums.ReviewStatus;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.ReviewRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Transactional
    public ReviewDto createReview(ReviewDto dto) {
        Order order = resolveOrder(dto.getOrderId());
        User reviewer = resolveUser(getReviewerId(dto), "Reviewer");
        User reviewee = resolveUser(getRevieweeId(dto), "Reviewee");

        validatePendingReviewCreation(order, reviewer, reviewee);
        if (dto.getRating() != null || dto.getComment() != null || dto.getStatus() == ReviewStatus.COMPLETED) {
            throw new IllegalArgumentException("Pending reviews must be completed through the update endpoint");
        }

        Review savedReview = repository.save(buildPendingReview(order, reviewer, reviewee));
        return mapToDto(savedReview);
    }

    @Override
    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto dto) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (review.getStatus() == ReviewStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed reviews cannot be updated");
        }

        validateImmutableFields(review, dto);

        Integer rating = dto.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        review.setRating(rating);
        review.setComment(dto.getComment() != null ? dto.getComment().trim() : null);
        review.setStatus(ReviewStatus.COMPLETED);

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
    @Transactional
    public void deleteReview(Long id) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        User reviewee = review.getReviewee();
        repository.delete(review);
        updateTrustScore(reviewee);
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

    @Override
    public List<ReviewDto> getReviewsByOrderId(Long orderId) {
        return repository.findByOrder_Id(orderId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getPendingReviewsByReviewerId(Long reviewerId) {
        return repository.findByReviewer_IdAndStatus(reviewerId, ReviewStatus.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createPendingReviewsForOrder(Order order) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (!OrderStatus.DELIVERED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Pending reviews can only be created for delivered orders");
        }

        List<User> participants = new ArrayList<>(participantMap(order).values());
        for (User reviewer : participants) {
            for (User reviewee : participants) {
                if (reviewer == null || reviewee == null || Objects.equals(reviewer.getId(), reviewee.getId())) {
                    continue;
                }
                if (!repository.existsByOrder_IdAndReviewer_IdAndReviewee_Id(order.getId(), reviewer.getId(), reviewee.getId())) {
                    repository.save(buildPendingReview(order, reviewer, reviewee));
                }
            }
        }
    }

    public ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();

        dto.setId(review.getId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setOrderId(review.getOrder() != null ? review.getOrder().getId() : null);
        dto.setStatus(review.getStatus());
        dto.setReviewerId(review.getReviewer() != null ? review.getReviewer().getId() : null);
        dto.setRevieweeId(review.getReviewee() != null ? review.getReviewee().getId() : null);
        dto.setReviewer(mapUserToResponse(review.getReviewer()));
        dto.setReviewee(mapUserToResponse(review.getReviewee()));

        return dto;
    }

    private Review buildPendingReview(Order order, User reviewer, User reviewee) {
        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        review.setRating(null);
        review.setComment(null);
        return review;
    }

    private void validatePendingReviewCreation(Order order, User reviewer, User reviewee) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (!OrderStatus.DELIVERED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Reviews are only allowed after delivery");
        }
        validateParticipants(order, reviewer, reviewee);
        if (repository.existsByOrder_IdAndReviewer_IdAndReviewee_Id(order.getId(), reviewer.getId(), reviewee.getId())) {
            throw new IllegalArgumentException("Review already exists for this order and user pair");
        }
    }

    private void validateParticipants(Order order, User reviewer, User reviewee) {
        if (reviewer == null || reviewee == null) {
            throw new IllegalArgumentException("Reviewer and reviewee are required");
        }
        if (reviewer.getId() == null || reviewee.getId() == null) {
            throw new IllegalArgumentException("Reviewer and reviewee IDs are required");
        }
        if (Objects.equals(reviewer.getId(), reviewee.getId())) {
            throw new IllegalArgumentException("Reviewer and reviewee must be different users");
        }
        if (!isParticipant(order, reviewer) || !isParticipant(order, reviewee)) {
            throw new IllegalArgumentException("Both users must be participants in the order");
        }
    }

    private void validateImmutableFields(Review review, ReviewDto dto) {
        Long dtoOrderId = dto.getOrderId();
        if (dtoOrderId != null && !Objects.equals(dtoOrderId, review.getOrder() != null ? review.getOrder().getId() : null)) {
            throw new IllegalArgumentException("Order cannot be changed when completing a review");
        }

        Long reviewerId = getReviewerId(dto);
        if (reviewerId != null && !Objects.equals(reviewerId, review.getReviewer() != null ? review.getReviewer().getId() : null)) {
            throw new IllegalArgumentException("Reviewer cannot be changed when completing a review");
        }

        Long revieweeId = getRevieweeId(dto);
        if (revieweeId != null && !Objects.equals(revieweeId, review.getReviewee() != null ? review.getReviewee().getId() : null)) {
            throw new IllegalArgumentException("Reviewee cannot be changed when completing a review");
        }

        if (dto.getStatus() != null && dto.getStatus() != ReviewStatus.COMPLETED && dto.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalArgumentException("Invalid review status");
        }
    }

    private void updateTrustScore(User user) {
        if (user == null) {
            return;
        }
        Double avg = repository.findAverageRatingForUser(user.getId());
        user.setTrustScore(avg == null ? 0 : (int) Math.round(avg));
        userRepository.save(user);
    }

    private User resolveUser(Long userId, String label) {
        if (userId == null) {
            throw new IllegalArgumentException(label + " ID is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found with id: " + userId));
    }

    private Long getReviewerId(ReviewDto dto) {
        if (dto.getReviewerId() != null) {
            return dto.getReviewerId();
        }
        return dto.getReviewer() != null ? dto.getReviewer().getId() : null;
    }

    private Long getRevieweeId(ReviewDto dto) {
        if (dto.getRevieweeId() != null) {
            return dto.getRevieweeId();
        }
        return dto.getReviewee() != null ? dto.getReviewee().getId() : null;
    }

    private boolean isParticipant(Order order, User user) {
        if (order == null || user == null || user.getId() == null) {
            return false;
        }
        return participantMap(order).containsKey(user.getId());
    }

    private Map<Long, User> participantMap(Order order) {
        Map<Long, User> participants = new LinkedHashMap<>();
        addParticipant(participants, order.getBuyer());
        addParticipant(participants, order.getFarmer());
        if (order.getLogisticsRequest() != null) {
            addParticipant(participants, order.getLogisticsRequest().getAssignedProvider());
        }
        return participants;
    }

    private void addParticipant(Map<Long, User> participants, User user) {
        if (user != null && user.getId() != null) {
            participants.putIfAbsent(user.getId(), user);
        }
    }

    private Order resolveOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private UserResponseDTO mapUserToResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAddress(user.getAddress());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setVerified(user.getVerified());
        dto.setTrustScore(user.getTrustScore());
        dto.setUsdBalance(user.getUsdBalance());
        dto.setZigBalance(user.getZigBalance());

        switch (user) {
            case Farmer farmer -> {
                dto.setRole("FARMER");
                dto.setFarmName(farmer.getFarmName());
                dto.setFarmLocation(farmer.getFarmLocation());
                dto.setSuccessfulSales(farmer.getSuccessfulSales());
                dto.setUnsuccessfulSales(farmer.getUnsuccessfulSales());
            }
            case Buyer buyer -> {
                dto.setRole("BUYER");
                dto.setCompanyName(buyer.getCompanyName());
                dto.setSuccessfulBuys(buyer.getSuccessfulBuys());
                dto.setUnsuccessfulBuys(buyer.getUnsuccessfulBuys());
            }
            case LogisticsProvider provider -> {
                dto.setRole("LOGISTICS");
                dto.setLicenseNumber(provider.getLicenseNumber());
                dto.setDefensiveId(provider.getDefensiveId());
            }
            default -> dto.setRole(user.getRole());
        }

        return dto;
    }
}
