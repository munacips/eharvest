package com.munashechipanga.eharvest.reports;

import com.munashechipanga.eharvest.reports.dto.ReportDescriptor;
import com.munashechipanga.eharvest.reports.dto.ReportRequestParams;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ReportService {

    /**
     * Returns the report descriptors visible to the authenticated user.
     *
     * @param auth the authenticated principal
     * @return filtered report descriptors
     */
    List<ReportDescriptor> availableReportsForUser(Authentication auth);

    /**
     * Generates a PDF for the requested report if the user is allowed to access it.
     *
     * @param reportName unique registry key
     * @param params request parameters used to scope the data set
     * @param auth the authenticated principal
     * @return PDF bytes
     */
    byte[] generatePdf(String reportName, ReportRequestParams params, Authentication auth);
}
