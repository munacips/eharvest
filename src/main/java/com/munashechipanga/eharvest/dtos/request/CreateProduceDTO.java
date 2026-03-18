package com.munashechipanga.eharvest.dtos.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateProduceDTO {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String qualityGrade;
    private Double quantity;
    private Double price;
    private LocalDate availableFrom;
    private LocalDate harvestDate;
    private Long farmer;
    private List<String> imageUrls = new ArrayList<>();
}
