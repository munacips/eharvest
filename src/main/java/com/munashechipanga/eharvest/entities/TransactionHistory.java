package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.entities.User;

@Entity
@Getter
@Setter
public class TransactionHistory {
    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime transactionDate;
    private Double amount;
    private String status;
    private String transactionReference;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    private String provider;
    private String providerReference;

    @ManyToOne
    private User user;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
