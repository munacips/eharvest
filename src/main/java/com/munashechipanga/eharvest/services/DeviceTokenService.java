package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.entities.DeviceToken;
import com.munashechipanga.eharvest.repositories.DeviceTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    /**
     * Register or update a device token for a user
     */
    public DeviceToken registerToken(Long userId, String fcmToken, String deviceType) {
        // Check if token already exists
        var existingToken = deviceTokenRepository.findByFcmToken(fcmToken);

        if (existingToken.isPresent()) {
            DeviceToken token = existingToken.get();
            token.setIsActive(true);
            log.info("Updated existing device token for user: {}", userId);
            return deviceTokenRepository.save(token);
        }

        DeviceToken newToken = DeviceToken.builder()
                .userId(userId)
                .fcmToken(fcmToken)
                .deviceType(deviceType)
                .isActive(true)
                .build();

        log.info("Registered new device token for user: {} on device: {}", userId, deviceType);
        return deviceTokenRepository.save(newToken);
    }

    /**
     * Deactivate a device token
     */
    public void deactivateToken(String fcmToken) {
        var token = deviceTokenRepository.findByFcmToken(fcmToken);

        if (token.isPresent()) {
            DeviceToken deviceToken = token.get();
            deviceToken.setIsActive(false);
            deviceTokenRepository.save(deviceToken);
            log.info("Deactivated device token");
        } else {
            log.warn("Device token not found for deactivation: {}", fcmToken);
        }
    }

    /**
     * Get all active tokens for a user
     */
    public List<DeviceToken> getActiveTokensByUserId(Long userId) {
        return deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get all tokens for a user (active and inactive)
     */
    public List<DeviceToken> getTokensByUserId(Long userId) {
        return deviceTokenRepository.findByUserId(userId);
    }
}
