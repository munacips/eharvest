package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationDispatchService {

    private final PushNotificationService pushNotificationService;
    private final SmsService smsService;
    private final UserRepository userRepository;

    public NotificationDispatchService(PushNotificationService pushNotificationService,
            SmsService smsService,
            UserRepository userRepository) {
        this.pushNotificationService = pushNotificationService;
        this.smsService = smsService;
        this.userRepository = userRepository;
    }

    /**
     * Send both push and SMS notifications to a user
     * Sends push notification to all registered devices
     * Sends SMS if user has a phone number on record
     */
    public void notifyUser(Long userId, String title, String body) {
        try {
            // Send push notification
            Map<String, String> data = new HashMap<>();
            pushNotificationService.sendToUser(userId, title, body, data);

            // Send SMS if phone number is available
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
                String smsMessage = title + ": " + body;
                smsService.sendSms(user.getPhoneNumber(), smsMessage);
                log.info("Notifications sent to user: {} via push and SMS", userId);
            } else {
                log.warn("User {} has no phone number on record for SMS notification", userId);
                log.info("Push notification sent to user: {} (SMS skipped)", userId);
            }
        } catch (ResourceNotFoundException e) {
            log.error("User not found: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error sending notifications to user: {}", userId, e);
        }
    }
}
