package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.request.DisputeReportRequestDTO;
import com.munashechipanga.eharvest.dtos.response.DisputeReportResponseDTO;
import com.munashechipanga.eharvest.services.DisputeReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/dispute-reports")
public class DisputeReportController {

    @Autowired
    private DisputeReportService disputeReportService;

    @PostMapping
    public ResponseEntity<DisputeReportResponseDTO> createReport(@RequestBody DisputeReportRequestDTO dto,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(disputeReportService.createReport(authentication.getName(), dto));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DisputeReportResponseDTO>> getMyReports(Authentication authentication) {
        return ResponseEntity.ok(disputeReportService.getReportsByFiledByUsername(authentication.getName()));
    }
}
