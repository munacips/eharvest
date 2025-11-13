package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        User user;

        String password = passwordEncoder.encode(dto.getPassword());

        switch (dto.getRole().toUpperCase()) {
            case "FARMER":
                Farmer farmer = new Farmer();
                farmer.setFarmName(dto.getFarmName());
                farmer.setFarmLocation(dto.getFarmLocation());
                user = farmer;
                break;
            case "BUYER":
                Buyer buyer = new Buyer();
                buyer.setCompanyName(dto.getCompanyName());
                user = buyer;
                break;
            case "LOGISTICS":
                LogisticsProvider lp = new LogisticsProvider();
                lp.setLicenseNumber(dto.getLicenseNumber());
                user = lp;
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }

        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setNationalId(dto.getNationalId());
        user.setAddress(dto.getAddress());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(password);
        user.setVerified(false);
        user.setTrustScore(0);
        user.setActive(true);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getId()));

        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getNationalId() != null) user.setNationalId(dto.getNationalId());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getVerified() != null) user.setVerified(dto.getVerified());
        if (dto.getTrustScore() != null) user.setTrustScore(dto.getTrustScore());
        if (dto.getActive() != null) user.setActive(dto.getActive());

        if (user instanceof Farmer) {
            Farmer f = (Farmer) user;
            if (dto.getFarmName() != null) f.setFarmName(dto.getFarmName());
            if (dto.getFarmLocation() != null) f.setFarmLocation(dto.getFarmLocation());
            if (dto.getSuccessfulBuys() != null) f.setSuccessfulSales(dto.getSuccessfulBuys());
            if (dto.getUnsuccessfulSales() != null) f.setUnsuccessfulSales(dto.getUnsuccessfulSales());
        } else if (user instanceof Buyer) {
            Buyer b = (Buyer) user;
            if (dto.getCompanyName() != null) b.setCompanyName(dto.getCompanyName());
            if (dto.getSuccessfulBuys() != null) b.setSuccessfulBuys(dto.getSuccessfulBuys());
            if (dto.getUnsuccessfulBuys() != null) b.setUnsuccessfulBuys(dto.getUnsuccessfulBuys());
        } else if (user instanceof LogisticsProvider) {
            LogisticsProvider lp = (LogisticsProvider) user;
            if (dto.getLicenseNumber() != null) lp.setLicenseNumber(dto.getLicenseNumber());
        }

        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id : " + id + " not found"));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserResponseDTO mapToResponse(User user) {
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

        if (user instanceof Farmer) {
            dto.setRole("FARMER");
            dto.setFarmName(((Farmer) user).getFarmName());
            dto.setFarmLocation(((Farmer) user).getFarmLocation());
            dto.setSuccessfulBuys(((Farmer) user).getSuccessfulSales());
            dto.setUnsuccessfulSales(((Farmer) user).getUnsuccessfulSales());
        } else if (user instanceof Buyer) {
            dto.setRole("BUYER");
            dto.setCompanyName(((Buyer) user).getCompanyName());
            dto.setSuccessfulBuys(((Buyer) user).getSuccessfulBuys());
            dto.setUnsuccessfulBuys(((Buyer) user).getUnsuccessfulBuys());
        } else if (user instanceof LogisticsProvider) {
            dto.setRole("LOGISTICS");
            dto.setLicenseNumber(((LogisticsProvider) user).getLicenseNumber());
        }

        return dto;
    }
}
