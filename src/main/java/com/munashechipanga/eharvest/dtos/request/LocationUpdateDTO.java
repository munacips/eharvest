package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationUpdateDTO {
    private Long orderId;
    private Long providerId;
    private Double latitude;
    private Double longitude;
    private Double heading;
    private Double speed;
}
