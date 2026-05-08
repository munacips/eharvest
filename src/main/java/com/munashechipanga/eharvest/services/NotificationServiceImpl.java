package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.NotificationDto;
import com.munashechipanga.eharvest.entities.Notification;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.enums.NotificationChannel;
import com.munashechipanga.eharvest.enums.NotificationStatus;
import com.munashechipanga.eharvest.enums.NotificationType;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.NotificationRepository;
import com.munashechipanga.eharvest.services.notifications.EmailNotificationSender;
import com.munashechipanga.eharvest.services.notifications.PushNotificationSender;
import com.munashechipanga.eharvest.services.notifications.SmsNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;
    private final SmsService smsService;
    private final PushNotificationSender pushSender;
    private final EmailNotificationSender emailSender;
    private final SmsNotificationSender smsSender;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
            PushNotificationService pushNotificationService,
            SmsService smsService,
            PushNotificationSender pushSender,
            EmailNotificationSender emailSender,
            SmsNotificationSender smsSender) {
        this.notificationRepository = notificationRepository;
        this.pushNotificationService = pushNotificationService;
        this.smsService = smsService;
        this.pushSender = pushSender;
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }

    @Override
    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDto markRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return mapToDto(notificationRepository.save(notification));
    }

    @Override
    public void sendOrderUpdate(User user, String title, String message) {
        sendToAll(user, title, message, NotificationType.ORDER);
    }

    @Override
    public void sendPaymentUpdate(User user, String title, String message) {
        sendToAll(user, title, message, NotificationType.PAYMENT);
    }

    @Override
    public void sendLogisticsUpdate(User user, String title, String message) {
        sendToAll(user, title, message, NotificationType.LOGISTICS);
    }

    @Override
    public void sendVerification(User user, String title, String message) {
        sendToAll(user, title, message, NotificationType.VERIFICATION);
    }

    @Override
    public void sendSystem(User user, String title, String message) {
        sendToAll(user, title, message, NotificationType.SYSTEM);
    }

    private void sendToAll(User user, String title, String message, NotificationType type) {
        List<NotificationChannel> channels = List.of(
                NotificationChannel.PUSH,
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.IN_APP);

        for (NotificationChannel channel : channels) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setChannel(channel);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());

            try {
                dispatch(channel, user, title, message);
                notification.setStatus(NotificationStatus.SENT);
            } catch (Exception ex) {
                log.error("Failed to send notification via {}: ", channel, ex);
                notification.setStatus(NotificationStatus.FAILED);
            }

            notificationRepository.save(notification);
        }
    }

    private void dispatch(NotificationChannel channel, User user, String title, String message) {
        switch (channel) {
            case PUSH -> {
                pushSender.send(user, title, message);
                if (user.getId() != null) {
                    pushNotificationService.sendToUser(user.getId(), title, message, new HashMap<>());
                }
            }
            case EMAIL -> emailSender.send(user, title, message);
            case SMS -> {
                smsSender.send(user, title, message);
                if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
                    smsService.sendSms(user.getPhoneNumber(), message);
                }
            }
            case IN_APP -> {
                // In-app notifications are already stored in DB
            }
        }
    }

    private NotificationDto mapToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser() != null ? notification.getUser().getId() : null);
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setChannel(notification.getChannel());
        dto.setStatus(notification.getStatus());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }
}
