package com.munashechipanga.eharvest.dtos.chat;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateConversationRequest {
    private List<Long> memberIds;
    private String name;
    private Boolean isGroup;
}
