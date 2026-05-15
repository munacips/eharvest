package com.munashechipanga.eharvest.dtos.chat;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class MemberDTO {
    private Long userId;
    private String fullName;
    private LocalDateTime joinedAt;
    private LocalDateTime lastReadAt;
}
