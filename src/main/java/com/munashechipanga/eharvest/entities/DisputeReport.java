package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispute_reports")
@Getter
@Setter
public class DisputeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "filed_by_id")
    private User filedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "filed_against_id")
    private User filedAgainst;

    private Boolean attendedTo = false;

    private LocalDateTime createdAt;
}
