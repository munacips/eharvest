package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.FarmerDto;
import com.munashechipanga.eharvest.dtos.FarmerFilter;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FarmerService {
    UserResponseDTO getFarmerById(Long id);
    UserResponseDTO createFarmer(FarmerDto dto);
    UserResponseDTO updateFarmer(Long id,FarmerDto dto);
    void deleteFarmer(Long id);
    List<UserResponseDTO> getAllFarmers();

    Page<UserResponseDTO> search(FarmerFilter filter, Pageable pageable);
}
