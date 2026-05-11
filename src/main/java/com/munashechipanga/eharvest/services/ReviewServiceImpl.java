package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
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
    public ReviewDto createReview(ReviewDto dto) {
        User reviewer = resolveUser(getReviewerId(dto), "Reviewer");
        User reviewee = resolveUser(getRevieweeId(dto), "Reviewee");
        validateReview(reviewer, reviewee, dto.getRating());

        Review review = new Review();

        review.setRating(dto.getRating());
        review.setReviewer(reviewer);
        review.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.LocalDateTime.now());
        review.setReviewee(reviewee);
        review.setComment(dto.getComment());

        Review savedReview = repository.save(review);
        updateTrustScore(savedReview.getReviewee());
        return mapToDto(savedReview);
    }

    @Override
    public ReviewDto updateReview(Long id, ReviewDto dto) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        User oldReviewee = review.getReviewee();

        Long reviewerId = getReviewerId(dto);
        Long revieweeId = getRevieweeId(dto);

        if(reviewerId != null) review.setReviewer(resolveUser(reviewerId, "Reviewer"));
        if(dto.getComment() != null) review.setComment(dto.getComment());
        if(revieweeId != null) review.setReviewee(resolveUser(revieweeId, "Reviewee"));
        if(dto.getCreatedAt() != null) review.setCreatedAt(dto.getCreatedAt());
        if(dto.getRating() != null) review.setRating(dto.getRating());

        validateReview(review.getReviewer(), review.getReviewee(), review.getRating());

        Review savedReview = repository.save(review);
        if (!sameUser(oldReviewee, savedReview.getReviewee())) {
            updateTrustScore(oldReviewee);
        }
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

    public ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();

        dto.setId(review.getId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setReviewerId(review.getReviewer() != null ? review.getReviewer().getId() : null);
        dto.setRevieweeId(review.getReviewee() != null ? review.getReviewee().getId() : null);
        dto.setReviewer(mapUserToResponse(review.getReviewer()));
        dto.setReviewee(mapUserToResponse(review.getReviewee()));

        return dto;
    }

    private void validateReview(User reviewer, User reviewee, Integer rating) {
        if (reviewer == null || reviewee == null) {
            throw new IllegalArgumentException("Reviewer and reviewee are required");
        }

        if (reviewer.getId() == null || reviewee.getId() == null) {
            throw new IllegalArgumentException("Reviewer and reviewee IDs are required");
        }

        if (Objects.equals(reviewer.getId(), reviewee.getId())) {
            throw new IllegalArgumentException("Reviewer and reviewee must be different users");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

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
        if (dto.getReviewerId() != null) return dto.getReviewerId();
        return dto.getReviewer() != null ? dto.getReviewer().getId() : null;
    }

    private Long getRevieweeId(ReviewDto dto) {
        if (dto.getRevieweeId() != null) return dto.getRevieweeId();
        return dto.getReviewee() != null ? dto.getReviewee().getId() : null;
    }

    private boolean sameUser(User left, User right) {
        if (left == null || right == null) {
            return left == right;
        }
        return Objects.equals(left.getId(), right.getId());
    }

    private UserResponseDTO mapUserToResponse(User user) {
        if (user == null) return null;

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
