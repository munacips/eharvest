package com.munashechipanga.eharvest.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenRegisterRequest {

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("fcmToken")
    private String fcmToken;

    @JsonProperty("deviceType")
    private String deviceType; // "android" or "ios"
}
