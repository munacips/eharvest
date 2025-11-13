package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LogisticsRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pickupLocation;
    private String deliveryLocation;
    private String status; // AWAITING_PICKUP, IN_TRANSIT, DELIVERED
    private Double cost;

    @ManyToOne
    private LogisticsProvider assignedProvider;

    @ManyToOne
    private Order order;
}
