package com.munashechipanga.eharvest.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisputeReportRequestDTO {
    private String description;
    private Long filedAgainstId;
}
