package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Produce {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String description;
    private String qualityGrade;
    private double quantity;
    private double price;
    private LocalDate availableFrom;
    private LocalDate harvestDate;

    @ManyToOne
    private Farmer farmer;

}
