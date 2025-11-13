package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.FarmerDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.FarmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmerServiceImpl implements  FarmerService {

    @Autowired
    FarmerRepository farmerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO getFarmerById(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
        return mapToResponse(farmer);
    }

    @Override
    public UserResponseDTO createFarmer(FarmerDto dto) {
        Farmer farmer = new Farmer();

        String password = passwordEncoder.encode(dto.getPassword());

        farmer.setFirstName(dto.getFirstName());
        farmer.setLastName(dto.getLastName());
        farmer.setAddress(dto.getAddress());
        farmer.setEmail(dto.getEmail());
        farmer.setUsername(dto.getUsername());
        farmer.setNationalId(dto.getNationalId());
        farmer.setRole("farmer");
        farmer.setEmail(dto.getEmail());
        farmer.setPassword(password);
        farmer.setPhoneNumber(dto.getPhoneNumber());
        farmer.setAddress(dto.getAddress());
        farmer.setActive(dto.getActive());
        farmer.setVerified(dto.getVerified());
        farmer.setTrustScore(dto.getTrustScore());
        farmer.setFarmName(dto.getFarmName());
        farmer.setFarmLocation(dto.getFarmLocation());
        farmer.setSuccessfulSales(dto.getSuccessfulSales());
        farmer.setUnsuccessfulSales(dto.getUnsuccessfulSales());

        Farmer newFarmer = farmerRepository.save(farmer);

        return mapToResponse(newFarmer);
    }

    @Override
    public UserResponseDTO updateFarmer(Long id, FarmerDto dto) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        if (dto.getFirstName() != null) farmer.setFirstName(dto.getFirstName());
        if (dto.getPhoneNumber() != null) farmer.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getActive() != null) farmer.setActive(dto.getActive());
        if (dto.getVerified() != null) farmer.setVerified(dto.getVerified());
        if (dto.getTrustScore() != null) farmer.setTrustScore(dto.getTrustScore());
        if (dto.getFarmName() != null) farmer.setFarmName(dto.getFarmName());
        if (dto.getFarmLocation() != null) farmer.setFarmLocation(dto.getFarmLocation());
        if (dto.getSuccessfulSales() != null) farmer.setSuccessfulSales(dto.getSuccessfulSales());
        if (dto.getUnsuccessfulSales() != null) farmer.setUnsuccessfulSales(dto.getUnsuccessfulSales());
        if (dto.getLastName() != null) farmer.setLastName(dto.getLastName());
        if (dto.getAddress() != null) farmer.setAddress(dto.getAddress());
        if (dto.getEmail() != null) farmer.setEmail(dto.getEmail());
        if (dto.getUsername() != null) farmer.setUsername(dto.getUsername());
        if (dto.getNationalId() != null) farmer.setNationalId(dto.getNationalId());
        if (dto.getRole() != null) farmer.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            farmer.setPassword(encodedPassword);
        }


        return mapToResponse(farmerRepository.save(farmer));
    }

    @Override
    public void deleteFarmer(Long id) {
        farmerRepository.deleteById(id);
    }

    @Override
    public List<UserResponseDTO> getAllFarmers() {
        return farmerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserResponseDTO mapToResponse(Farmer user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAddress(user.getAddress());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setVerified(user.getVerified());
        dto.setTrustScore(user.getTrustScore());
        dto.setRole("FARMER");
        dto.setFarmLocation(user.getFarmLocation());
        dto.setFarmName(user.getFarmName());
        dto.setSuccessfulSales(user.getSuccessfulSales());
        dto.setUnsuccessfulSales(user.getUnsuccessfulSales());
        return dto;
    }
}
