package com.munashechipanga.eharvest.reports;

import com.munashechipanga.eharvest.reports.dto.ReportDescriptor;
import com.munashechipanga.eharvest.reports.dto.ReportRequestParams;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/available")
    public List<ReportDescriptor> availableReports(Authentication authentication) {
        return reportService.availableReportsForUser(authentication);
    }

    @GetMapping("/generate/{reportName}")
    public ResponseEntity<byte[]> generateReport(@PathVariable String reportName,
                                                 @ModelAttribute ReportRequestParams params,
                                                 Authentication authentication) {
        byte[] pdf = reportService.generatePdf(reportName, params, authentication);

        String from = params.getFrom() != null ? params.getFrom().toString() : "all";
        String to = params.getTo() != null ? params.getTo().toString() : "all";
        String filename = "%s_%s_%s.pdf".formatted(reportName, from, to);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
}
