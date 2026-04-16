package com.munashechipanga.eharvest.services.notifications;

import com.munashechipanga.eharvest.entities.User;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationSender implements NotificationSender {
    @Override
    public void send(User user, String title, String message) {
        System.out.println("SMS to " + user.getPhoneNumber() + ": " + title + " - " + message);
    }
}
