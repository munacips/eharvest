package com.munashechipanga.eharvest.services;

import com.google.firebase.messaging.*;
import com.munashechipanga.eharvest.entities.DeviceToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    private final DeviceTokenService deviceTokenService;

    public PushNotificationService(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * Send push notification to a specific user
     */
    public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
        List<DeviceToken> activeTokens = deviceTokenService.getActiveTokensByUserId(userId);

        if (activeTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", userId);
            return;
        }

        for (DeviceToken token : activeTokens) {
            sendToToken(token.getFcmToken(), title, body, data, token.getId());
        }
    }

    /**
     * Send push notification to a topic
     */
    public void sendToTopic(String topic, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setNotification(notification)
                    .setTopic(topic)
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message to topic [{}]: {}", topic, messageId);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send message to topic [{}]: ", topic, e);
        }
    }

    /**
     * Send notification to a specific FCM token
     */
    private void sendToToken(String fcmToken, String title, String body, Map<String, String> data, Long tokenId) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent push notification to token [{}]: {}", fcmToken, messageId);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to token [{}]: ", fcmToken, e);

            // Handle specific error cases
            if (isInvalidTokenError(e)) {
                log.info("Deactivating invalid token: {}", fcmToken);
                deviceTokenService.deactivateToken(fcmToken);
            }
        }
    }

    /**
     * Check if the error is due to an invalid or expired token
     */
    private boolean isInvalidTokenError(FirebaseMessagingException e) {
        String message = e.getMessage();
        return message != null && (message.contains("invalid registration token") ||
                message.contains("registration token is invalid") ||
                message.contains("Third party authentication failure") ||
                message.contains("unregistered"));
    }
}
