package com.munashechipanga.eharvest.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Boolean active;
    private Boolean verified;
    private Integer trustScore;
    private String role;

    // Farmers
    private String farmName;
    private String farmLocation;
    private Integer successfulSales;
    private Integer unsuccessfulSales;

    // Buyers
    private String companyName;
    private Integer successfulBuys;
    private Integer unsuccessfulBuys;

    // For Logistics providers
    private String licenseNumber;
    private String defensiveId;

    private Double usdBalance;
    private Double zigBalance;

}
