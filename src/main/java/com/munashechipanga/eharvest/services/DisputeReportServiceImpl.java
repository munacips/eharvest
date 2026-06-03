package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.DisputeReportRequestDTO;
import com.munashechipanga.eharvest.dtos.response.DisputeReportResponseDTO;
import com.munashechipanga.eharvest.entities.DisputeReport;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.DisputeReportRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisputeReportServiceImpl implements DisputeReportService {

    @Autowired
    private DisputeReportRepository disputeReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public DisputeReportResponseDTO createReport(String filedByUsername, DisputeReportRequestDTO dto) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (dto.getFiledAgainstId() == null) {
            throw new IllegalArgumentException("filedAgainstId is required");
        }

        User filedBy = userRepository.findByUsername(filedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + filedByUsername));
        User filedAgainst = userRepository.findById(dto.getFiledAgainstId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getFiledAgainstId()));

        if (filedBy.getId().equals(filedAgainst.getId())) {
            throw new IllegalArgumentException("You cannot file a report against yourself");
        }

        DisputeReport report = new DisputeReport();
        report.setDescription(dto.getDescription().trim());
        report.setFiledBy(filedBy);
        report.setFiledAgainst(filedAgainst);
        report.setAttendedTo(false);
        report.setCreatedAt(LocalDateTime.now());

        return mapToResponse(disputeReportRepository.save(report));
    }

    @Override
    public List<DisputeReportResponseDTO> getReportsByFiledByUsername(String filedByUsername) {
        User filedBy = userRepository.findByUsername(filedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + filedByUsername));
        return disputeReportRepository.findByFiledByIdOrderByCreatedAtDesc(filedBy.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DisputeReportResponseDTO> getAllReports(Boolean attendedTo) {
        List<DisputeReport> reports = attendedTo == null
                ? disputeReportRepository.findAllByOrderByCreatedAtDesc()
                : disputeReportRepository.findByAttendedToOrderByCreatedAtDesc(attendedTo);
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public DisputeReportResponseDTO getReportById(Long id) {
        DisputeReport report = disputeReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute report not found with id: " + id));
        return mapToResponse(report);
    }

    @Override
    public DisputeReportResponseDTO updateAttendedTo(Long id, boolean attendedTo) {
        DisputeReport report = disputeReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute report not found with id: " + id));
        report.setAttendedTo(attendedTo);
        return mapToResponse(disputeReportRepository.save(report));
    }

    @Override
    public void deleteReport(Long id) {
        if (!disputeReportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dispute report not found with id: " + id);
        }
        disputeReportRepository.deleteById(id);
    }

    private DisputeReportResponseDTO mapToResponse(DisputeReport report) {
        DisputeReportResponseDTO dto = new DisputeReportResponseDTO();
        dto.setId(report.getId());
        dto.setDescription(report.getDescription());
        dto.setFiledById(report.getFiledBy().getId());
        dto.setFiledByUsername(report.getFiledBy().getUsername());
        dto.setFiledAgainstId(report.getFiledAgainst().getId());
        dto.setFiledAgainstUsername(report.getFiledAgainst().getUsername());
        dto.setAttendedTo(report.getAttendedTo());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}
