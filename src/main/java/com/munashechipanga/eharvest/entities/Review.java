package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int rating; // 1 - 5
    private String comment;
    private LocalDateTime createdAt;

    @ManyToOne
    private User reviewer;

    @ManyToOne
    private User reviewee;
}
