package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.DisputeReportRequestDTO;
import com.munashechipanga.eharvest.dtos.response.DisputeReportResponseDTO;

import java.util.List;

public interface DisputeReportService {
    DisputeReportResponseDTO createReport(String filedByUsername, DisputeReportRequestDTO dto);

    List<DisputeReportResponseDTO> getReportsByFiledByUsername(String filedByUsername);

    List<DisputeReportResponseDTO> getAllReports(Boolean attendedTo);

    DisputeReportResponseDTO getReportById(Long id);

    DisputeReportResponseDTO updateAttendedTo(Long id, boolean attendedTo);

    void deleteReport(Long id);
}
