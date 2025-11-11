package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Order {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status; // PENDING, CONFIRMED, IN_TRANSIT, DELIVERED

    @ManyToOne
    private Buyer buyer;

    @OneToOne
    private LogisticsRequest logisticsRequest;

    private boolean escrowReleased;
}
