package com.munashechipanga.eharvest.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Buyer extends User{
    private String companyName;
    private int successfulBuys;
    private int unsuccessfulBuys;
}
