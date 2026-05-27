package com.munashechipanga.eharvest.entities;

import com.munashechipanga.eharvest.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "reviewer_id", "reviewee_id"})
})
@Getter
@Setter
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @ManyToOne
    private Order order;

    @ManyToOne
    private User reviewer;

    @ManyToOne
    private User reviewee;
}
