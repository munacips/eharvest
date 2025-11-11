package com.munashechipanga.eharvest.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LogisticsProvider extends User {
    private String licenseNumber;
    private String defensiveId;
}
