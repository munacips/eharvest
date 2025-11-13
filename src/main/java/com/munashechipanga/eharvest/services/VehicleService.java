package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.VehicleDto;
import com.munashechipanga.eharvest.entities.Vehicle;

import java.util.List;

public interface VehicleService {
    VehicleDto createVehicle(VehicleDto vehicle);
    VehicleDto updateVehicle(Long id, VehicleDto vehicle);
    VehicleDto getVehicleById(Long id);
    void deleteVehicle(Long id);
    List<VehicleDto> getAllVehicles();
}
