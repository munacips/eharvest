package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.ReviewDto;
import com.munashechipanga.eharvest.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/reviews")
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByReviewer(@PathVariable Long reviewerId) {
        return ResponseEntity.ok(reviewService.getReviewsByReviewerId(reviewerId));
    }

    @GetMapping("/reviewee/{revieweeId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByReviewee(@PathVariable Long revieweeId) {
        return ResponseEntity.ok(reviewService.getReviewsByRevieweeId(revieweeId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @RequestBody ReviewDto dto) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewDto> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}
