package com.munashechipanga.eharvest.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DisputeReportResponseDTO {
    private Long id;
    private String description;
    private Long filedById;
    private String filedByUsername;
    private Long filedAgainstId;
    private String filedAgainstUsername;
    private Boolean attendedTo;
    private LocalDateTime createdAt;
}
