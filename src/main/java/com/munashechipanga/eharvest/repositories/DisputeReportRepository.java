package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.DisputeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeReportRepository extends JpaRepository<DisputeReport, Long> {

    List<DisputeReport> findByFiledByIdOrderByCreatedAtDesc(Long filedById);

    List<DisputeReport> findByAttendedToOrderByCreatedAtDesc(Boolean attendedTo);

    List<DisputeReport> findAllByOrderByCreatedAtDesc();
}
