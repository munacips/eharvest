package com.munashechipanga.eharvest.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LocationPayloadDTO {
    private Long orderId;
    private Long providerId;
    private Double latitude;
    private Double longitude;
    private Double heading;
    private Double speed;
    private LocalDateTime timestamp;
}
