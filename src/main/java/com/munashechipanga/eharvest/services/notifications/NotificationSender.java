package com.munashechipanga.eharvest.services.notifications;

import com.munashechipanga.eharvest.entities.User;

public interface NotificationSender {
    void send(User user, String title, String message);
}
