package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;

import java.util.List;

public interface BuyerService {
    UserResponseDTO getBuyerById(Long id);
    UserResponseDTO createBuyer(BuyerDto buyerDto);
    UserResponseDTO updateBuyer(Long id,BuyerDto buyerDto);
    void deleteBuyer(Long id);
    List<UserResponseDTO> getAllBuyers();
}
