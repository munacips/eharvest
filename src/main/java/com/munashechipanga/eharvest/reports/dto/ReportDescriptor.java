package com.munashechipanga.eharvest.reports.dto;

import java.util.List;

public record ReportDescriptor(String reportName,
                               String label,
                               String description,
                               List<String> allowedRoles,
                               List<String> params) {
}
