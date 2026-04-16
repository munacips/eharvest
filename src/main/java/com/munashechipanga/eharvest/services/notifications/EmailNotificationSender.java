package com.munashechipanga.eharvest.services.notifications;

import com.munashechipanga.eharvest.entities.User;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationSender implements NotificationSender {
    @Override
    public void send(User user, String title, String message) {
        System.out.println("EMAIL to " + user.getEmail() + ": " + title + " - " + message);
    }
}
