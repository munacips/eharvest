package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.munashechipanga.eharvest.enums.Currency;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status; // PENDING, CONFIRMED, IN_TRANSIT, DELIVERED, REJECTED
    @Enumerated(EnumType.STRING)
    private Currency currency;
    private Boolean escrowHeld;
    private Double escrowAmount;

    @ManyToOne
    private Buyer buyer;

    @ManyToOne
    private Farmer farmer;

    @OneToOne
    private LogisticsRequest logisticsRequest;

    private Boolean escrowReleased;
}
