package com.munashechipanga.eharvest.dtos.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {
    private Long senderId;
    private Long conversationId;
    private String content;
}
