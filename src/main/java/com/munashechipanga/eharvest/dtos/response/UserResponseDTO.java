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
    private boolean active;
    private boolean verified;
    private int trustScore;
    private String role;

    // Farmers
    private String farmName;
    private String farmLocation;
    private int successfulSales;
    private int unsuccessfulSales;

    // Buyers
    private String companyName;
    private int successfulBuys;
    private int unsuccessfulBuys;

    // For Logistics providers
    private String licenseNumber;

}
