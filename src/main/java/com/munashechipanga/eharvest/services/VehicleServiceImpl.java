package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.VehicleDto;
import com.munashechipanga.eharvest.entities.Vehicle;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public VehicleDto createVehicle(VehicleDto dto) {

        Vehicle vehicle = new Vehicle();

        vehicle.setColour(dto.getColour());
        vehicle.setType(dto.getType());
        vehicle.setOwner(dto.getOwner());
        vehicle.setPlateNumber(dto.getPlateNumber());

        Vehicle newVehicle = vehicleRepository.save(vehicle);

        return mapToResponseDto(newVehicle);
    }

    @Override
    public VehicleDto updateVehicle(Long id, VehicleDto dto) {

        Vehicle vehicle =  vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Not Found"));

        if(dto.getColour() != null) vehicle.setColour(dto.getColour());
        if(dto.getType() != null) vehicle.setType(dto.getType());
        if(dto.getOwner() != null) vehicle.setOwner(dto.getOwner());
        if(dto.getPlateNumber() != null) vehicle.setPlateNumber(dto.getPlateNumber());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        return mapToResponseDto(updatedVehicle);
    }

    @Override
    public VehicleDto getVehicleById(Long id) {
        String message = "Vehicle with id : " + id + " not found";
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(message));
        return mapToResponseDto(vehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    @Override
    public List<VehicleDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private VehicleDto mapToResponseDto(Vehicle vehicle) {
        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setType(vehicle.getType());
        dto.setOwner(vehicle.getOwner());
        dto.setColour(vehicle.getColour());
        dto.setPlateNumber(vehicle.getPlateNumber());
        return dto;
    }
}
