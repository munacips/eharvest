package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.User;
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
    private User reviewer;
    private User reviewee;
}
