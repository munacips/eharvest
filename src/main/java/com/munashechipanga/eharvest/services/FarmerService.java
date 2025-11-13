package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.FarmerDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;

import java.util.List;

public interface FarmerService {
    UserResponseDTO getFarmerById(Long id);
    UserResponseDTO createFarmer(FarmerDto dto);
    UserResponseDTO updateFarmer(Long id,FarmerDto dto);
    void deleteFarmer(Long id);
    List<UserResponseDTO> getAllFarmers();
}
