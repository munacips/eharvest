package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;

@Entity
public class VehicleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageUrl;

    @ManyToOne
    private Vehicle vehicle;
}
