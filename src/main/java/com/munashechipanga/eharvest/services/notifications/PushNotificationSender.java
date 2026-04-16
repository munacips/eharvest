package com.munashechipanga.eharvest.services.notifications;

import com.munashechipanga.eharvest.entities.User;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationSender implements NotificationSender {
    @Override
    public void send(User user, String title, String message) {
        System.out.println("PUSH to " + user.getId() + ": " + title + " - " + message);
    }
}
