package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;// "FARMER", "BUYER", "LOGISTICS"
    private String nationalId;
    private String address;
    private Boolean active;
    private Boolean verified;
    private Integer trustScore;


    // For Farmers
    private String farmName;
    private String farmLocation;
    private Integer successfulSales;
    private Integer unsuccessfulSales;

    // For Buyer
    private String companyName;
    private Integer successfulBuys;
    private Integer unsuccessfulBuys;

    // For Logistics Provider
    private String licenseNumber;
}
