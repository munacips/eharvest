package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.Farmer;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

import java.time.LocalDate;

@Getter
@Setter
public class ProduceDto {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String qualityGrade;
    private Double quantity;
    private Double price;
    private LocalDate availableFrom;
    private LocalDate harvestDate;
    private Farmer farmer;
    private List<String> imageUrls = new ArrayList<>();
}
