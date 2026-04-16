package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.enums.NotificationChannel;
import com.munashechipanga.eharvest.enums.NotificationStatus;
import com.munashechipanga.eharvest.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDto {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
