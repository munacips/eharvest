package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuyerServiceImpl implements BuyerService {

    @Autowired
    BuyerRepository buyerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO getBuyerById(Long id) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
        return mapToResponse(buyer);
    }

    @Override
    public UserResponseDTO createBuyer(BuyerDto buyerDto) {

        Buyer buyer = new Buyer();

        String password = passwordEncoder.encode(buyerDto.getPassword());

        buyer.setFirstName(buyerDto.getFirstName());
        buyer.setLastName(buyerDto.getLastName());
        buyer.setAddress(buyerDto.getAddress());
        buyer.setEmail(buyerDto.getEmail());
        buyer.setUsername(buyerDto.getUsername());
        buyer.setNationalId(buyerDto.getNationalId());
        buyer.setRole("BUYER");
        buyer.setEmail(buyerDto.getEmail());
        buyer.setPassword(password);
        buyer.setPhoneNumber(buyerDto.getPhoneNumber());
        buyer.setAddress(buyerDto.getAddress());
        buyer.setActive(buyerDto.getActive());
        buyer.setVerified(buyerDto.getVerified());
        buyer.setTrustScore(buyerDto.getTrustScore());
        buyer.setCompanyName(buyerDto.getCompanyName());
        buyer.setSuccessfulBuys(buyerDto.getSuccessfulBuys());
        buyer.setUnsuccessfulBuys(buyerDto.getUnsuccessfulBuys());

        Buyer newBuyer = buyerRepository.save(buyer);

        return mapToResponse(newBuyer);
    }

    @Override
    public UserResponseDTO updateBuyer(Long id, BuyerDto dto) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

        if (dto.getFirstName() != null) buyer.setFirstName(dto.getFirstName());
        if (dto.getPhoneNumber() != null) buyer.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getActive() != null) buyer.setActive(dto.getActive());
        if (dto.getVerified() != null) buyer.setVerified(dto.getVerified());
        if (dto.getTrustScore() != null) buyer.setTrustScore(dto.getTrustScore());
        if (dto.getCompanyName() != null) buyer.setCompanyName(dto.getCompanyName());
        if (dto.getSuccessfulBuys() != null) buyer.setSuccessfulBuys(dto.getSuccessfulBuys());
        if (dto.getUnsuccessfulBuys() != null) buyer.setUnsuccessfulBuys(dto.getUnsuccessfulBuys());
        if (dto.getLastName() != null) buyer.setLastName(dto.getLastName());
        if (dto.getAddress() != null) buyer.setAddress(dto.getAddress());
        if (dto.getEmail() != null) buyer.setEmail(dto.getEmail());
        if (dto.getUsername() != null) buyer.setUsername(dto.getUsername());
        if (dto.getNationalId() != null) buyer.setNationalId(dto.getNationalId());
        if (dto.getRole() != null) buyer.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            buyer.setPassword(encodedPassword);
        }


        return mapToResponse(buyerRepository.save(buyer));
    }

    @Override
    public void deleteBuyer(Long id) {
        buyerRepository.deleteById(id);
    }

    @Override
    public List<UserResponseDTO> getAllBuyers() {
        return buyerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserResponseDTO mapToResponse(Buyer user) {
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
        dto.setRole("BUYER");
        dto.setFarmLocation(user.getCompanyName());
        dto.setSuccessfulBuys(user.getSuccessfulBuys());
        dto.setUnsuccessfulSales(user.getUnsuccessfulBuys());
        return dto;
    }
}
