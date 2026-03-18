package com.munashechipanga.eharvest.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
public class ProduceFilter {
    private Double minPrice;
    private Double maxPrice;
    private String category;
    private String name;
    private String qualityGrade;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate harvestFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate harvestTo;

    private String search;

}
