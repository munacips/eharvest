package com.munashechipanga.eharvest.entities;

import com.munashechipanga.eharvest.enums.Currency;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Buyer buyer;

    @ManyToOne
    private Farmer farmer;

    private String status;
    private String frequency;
    private LocalDateTime startDate;
    private LocalDateTime nextDeliveryDate;
    private Boolean requiresLogistics;
    private String pickupAddress;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionItem> items = new ArrayList<>();
}
