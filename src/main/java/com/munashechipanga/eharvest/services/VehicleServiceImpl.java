package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.VehicleDto;
import com.munashechipanga.eharvest.dtos.VehicleFilter;
import com.munashechipanga.eharvest.entities.Vehicle;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.VehicleRepository;
import com.munashechipanga.eharvest.specs.VehicleSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.munashechipanga.eharvest.entities.VehicleImage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

        // Map incoming image URLs to VehicleImage entities
        applyVehicleImagesFromDto(vehicle, dto);

        Vehicle newVehicle = vehicleRepository.save(vehicle);

        return mapToResponseDto(newVehicle);
    }

    @Override
    public VehicleDto updateVehicle(Long id, VehicleDto dto) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Not Found"));

        if (dto.getColour() != null)
            vehicle.setColour(dto.getColour());
        if (dto.getType() != null)
            vehicle.setType(dto.getType());
        if (dto.getOwner() != null)
            vehicle.setOwner(dto.getOwner());
        if (dto.getPlateNumber() != null)
            vehicle.setPlateNumber(dto.getPlateNumber());

        // If imageUrls provided, replace images accordingly; if null, leave unchanged
        applyVehicleImagesFromDto(vehicle, dto);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        return mapToResponseDto(updatedVehicle);
    }

    @Override
    public VehicleDto getVehicleById(Long id) {
        String message = "Vehicle with id : " + id + " not found";
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message));
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

    @Override
    public Page<VehicleDto> search(VehicleFilter filter, Pageable pageable) {
        return vehicleRepository.findAll(VehicleSpecifications.withFilters(filter), pageable)
                .map(this::mapToResponseDto);
    }

    @Override
    @Transactional
    public VehicleDto addVehicleImage(Long vehicleId, String imageUrl) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        VehicleImage img = new VehicleImage();
        img.setImageUrl(imageUrl);
        img.setVehicle(v);

        v.getImages().add(img);

        // cascade=ALL ensures the image is persisted with the vehicle
        Vehicle saved = vehicleRepository.save(v);

        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteVehicleImage(Long vehicleId, Long imageId) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        boolean removed = v.getImages().removeIf(img -> imageId.equals(img.getId()));
        if (!removed) {
            throw new ResourceNotFoundException("Image not found for this vehicle");
        }

        // orphanRemoval=true triggers actual delete of the removed image
        vehicleRepository.save(v);
    }

    private VehicleDto mapToResponseDto(Vehicle vehicle) {
        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setType(vehicle.getType());
        dto.setOwner(vehicle.getOwner());
        dto.setColour(vehicle.getColour());
        dto.setPlateNumber(vehicle.getPlateNumber());
        if (vehicle.getImages() != null) {
            dto.setImageUrls(vehicle.getImages().stream()
                    .map(VehicleImage::getImageUrl)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private void applyVehicleImagesFromDto(Vehicle vehicle, VehicleDto dto) {
        if (dto.getImageUrls() == null) return;

        vehicle.getImages().clear();
        for (String url : dto.getImageUrls()) {
            VehicleImage vi = new VehicleImage();
            vi.setImageUrl(url);
            vi.setVehicle(vehicle);
            vehicle.getImages().add(vi);
        }
    }
}
