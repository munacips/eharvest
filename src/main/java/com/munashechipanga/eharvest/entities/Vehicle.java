package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;

@Entity
public class Vehicle {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true, nullable=false)
    private String plateNumber;
    private String type;
    private String colour;

    @ManyToOne
    private LogisticsProvider owner;
}
