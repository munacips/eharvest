package com.munashechipanga.eharvest.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Produce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String description;
    private String qualityGrade;
    private Double quantity;
    private Double price;
    private LocalDate availableFrom;
    private LocalDate harvestDate;

    @ManyToOne
    private Farmer farmer;

    @OneToMany(mappedBy = "produce", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProduceImage> images = new ArrayList<>();

}
