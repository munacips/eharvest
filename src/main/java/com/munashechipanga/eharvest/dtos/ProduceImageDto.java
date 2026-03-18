package com.munashechipanga.eharvest.dtos;


import com.munashechipanga.eharvest.entities.Produce;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProduceImageDto {
    private Long id;
    private String imageUrl;
    private Produce produce;
}
