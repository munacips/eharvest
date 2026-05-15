package com.munashechipanga.eharvest.dtos.chat;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ConversationDTO {
    private Long id;
    private String name;
    private Boolean isGroup;
    private LocalDateTime createdAt;
    private List<MemberDTO> members;
    private MessageDTO lastMessage;
    private long unreadCount;
}
