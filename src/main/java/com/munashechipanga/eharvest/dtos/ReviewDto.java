package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.enums.ReviewStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long orderId;
    private ReviewStatus status;
    private Long reviewerId;
    private Long revieweeId;
    private UserResponseDTO reviewer;
    private UserResponseDTO reviewee;
}
