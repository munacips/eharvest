package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.LocationPayloadDTO;
import com.munashechipanga.eharvest.dtos.request.LocationUpdateDTO;
import com.munashechipanga.eharvest.services.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/api/v1/tracking/location")
    @ResponseBody
    public ResponseEntity<LocationPayloadDTO> updateLocationRest(@RequestBody LocationUpdateDTO dto) {
        return ResponseEntity.ok(trackingService.updateLocation(dto));
    }

    @MessageMapping("/location")
    public void updateLocationWs(@Payload LocationUpdateDTO dto) {
        trackingService.updateLocation(dto);
    }

    @GetMapping("/api/v1/tracking/{orderId}")
    @ResponseBody
    public ResponseEntity<LocationPayloadDTO> getLastKnownLocation(@PathVariable Long orderId) {
        return ResponseEntity.ok(trackingService.getLastKnownLocation(orderId));
    }
}
