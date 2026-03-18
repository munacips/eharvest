package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.BuyerFilter;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BuyerService {
    UserResponseDTO getBuyerById(Long id);
    UserResponseDTO createBuyer(BuyerDto buyerDto);
    UserResponseDTO updateBuyer(Long id,BuyerDto buyerDto);
    void deleteBuyer(Long id);
    List<UserResponseDTO> getAllBuyers();

    Page<UserResponseDTO> search(BuyerFilter filter, Pageable pageable);
}
