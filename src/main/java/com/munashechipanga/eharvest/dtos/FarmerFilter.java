package com.munashechipanga.eharvest.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FarmerFilter {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String farmName;
    private String farmLocation;
    private Boolean active;
    private Boolean verified;
    private Integer minTrustScore;
    private Integer maxTrustScore;
    private String search; // matches name/username/email/farmName/location

}
