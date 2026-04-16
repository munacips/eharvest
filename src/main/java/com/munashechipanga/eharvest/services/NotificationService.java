package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.NotificationDto;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getUserNotifications(Long userId);
    NotificationDto markRead(Long notificationId);

    void sendOrderUpdate(User user, String title, String message);
    void sendPaymentUpdate(User user, String title, String message);
    void sendLogisticsUpdate(User user, String title, String message);
    void sendVerification(User user, String title, String message);
    void sendSystem(User user, String title, String message);
}
