package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.VehicleDto;
import com.munashechipanga.eharvest.dtos.VehicleFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VehicleService {
    VehicleDto createVehicle(VehicleDto vehicle);
    VehicleDto updateVehicle(Long id, VehicleDto vehicle);
    VehicleDto getVehicleById(Long id);
    void deleteVehicle(Long id);
    List<VehicleDto> getAllVehicles();
    VehicleDto addVehicleImage(Long vehicleId, String imageUrl);
    void deleteVehicleImage(Long vehicleId, Long imageId);

    Page<VehicleDto> search(VehicleFilter filter, Pageable pageable);
}
