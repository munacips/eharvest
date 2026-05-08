package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.DeviceTokenDeactivateRequest;
import com.munashechipanga.eharvest.dtos.DeviceTokenRegisterRequest;
import com.munashechipanga.eharvest.entities.DeviceToken;
import com.munashechipanga.eharvest.services.DeviceTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * Register a device token for push notifications
     * POST /api/notifications/register-token
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, Object>> registerToken(@RequestBody DeviceTokenRegisterRequest request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId is required"));
            }

            if (request.getFcmToken() == null || request.getFcmToken().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "fcmToken is required"));
            }

            if (request.getDeviceType() == null || request.getDeviceType().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "deviceType is required (android or ios)"));
            }

            DeviceToken token = deviceTokenService.registerToken(
                    request.getUserId(),
                    request.getFcmToken(),
                    request.getDeviceType());

            log.info("Device token registered for user: {}", request.getUserId());

            return ResponseEntity.ok(Map.of(
                    "message", "Device token registered successfully",
                    "tokenId", token.getId(),
                    "userId", token.getUserId(),
                    "deviceType", token.getDeviceType()));
        } catch (Exception e) {
            log.error("Error registering device token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register device token"));
        }
    }

    /**
     * Deactivate a device token
     * DELETE /api/notifications/deactivate-token
     */
    @DeleteMapping("/deactivate-token")
    public ResponseEntity<Map<String, Object>> deactivateToken(@RequestBody DeviceTokenDeactivateRequest request) {
        try {
            if (request.getFcmToken() == null || request.getFcmToken().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "fcmToken is required"));
            }

            deviceTokenService.deactivateToken(request.getFcmToken());
            log.info("Device token deactivated");

            return ResponseEntity.ok(Map.of(
                    "message", "Device token deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating device token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate device token"));
        }
    }
}
