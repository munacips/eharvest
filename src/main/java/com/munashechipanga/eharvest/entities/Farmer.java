package com.munashechipanga.eharvest.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Farmer extends User {
    private String farmName;
    private String farmLocation;
    private int successfulSales;
    private int unsuccessfulSales;
}
