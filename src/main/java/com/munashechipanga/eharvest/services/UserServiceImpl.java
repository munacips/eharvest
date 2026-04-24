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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TrustScoreClient trustScoreClient;

    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        User user;

        String password = passwordEncoder.encode(dto.getPassword());

        switch (dto.getRole().toUpperCase()) {
            case "FARMER":
                Farmer farmer = new Farmer();
                farmer.setFarmName(dto.getFarmName());
                farmer.setFarmLocation(dto.getFarmLocation());
                farmer.setUnsuccessfulSales(dto.getUnsuccessfulSales());
                farmer.setSuccessfulSales(dto.getSuccessfulSales());
                user = farmer;
                break;
            case "BUYER":
                Buyer buyer = new Buyer();
                buyer.setCompanyName(dto.getCompanyName());
                buyer.setSuccessfulBuys(dto.getSuccessfulBuys());
                buyer.setUnsuccessfulBuys(dto.getUnsuccessfulBuys());
                user = buyer;
                break;
            case "LOGISTICS":
                LogisticsProvider lp = new LogisticsProvider();
                lp.setLicenseNumber(dto.getLicenseNumber());
                lp.setDefensiveId(dto.getDefensiveId());
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
        user.setRole(dto.getRole());
        user.setUsdBalance(0.00);
        user.setZigBalance(0.00);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getId()));

        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());
        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if (dto.getNationalId() != null)
            user.setNationalId(dto.getNationalId());
        if (dto.getAddress() != null)
            user.setAddress(dto.getAddress());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getPassword() != null)
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getVerified() != null)
            user.setVerified(dto.getVerified());
        if (dto.getTrustScore() != null)
            user.setTrustScore(dto.getTrustScore());
        if (dto.getActive() != null)
            user.setActive(dto.getActive());
        if (dto.getUsdBalance() != null)
            user.setUsdBalance(dto.getUsdBalance());
        if (dto.getZigBalance() != null)
            user.setZigBalance(dto.getZigBalance());

        if (user instanceof Farmer) {
            Farmer f = (Farmer) user;
            if (dto.getFarmName() != null)
                f.setFarmName(dto.getFarmName());
            if (dto.getFarmLocation() != null)
                f.setFarmLocation(dto.getFarmLocation());
            if (dto.getSuccessfulBuys() != null)
                f.setSuccessfulSales(dto.getSuccessfulBuys());
            if (dto.getUnsuccessfulSales() != null)
                f.setUnsuccessfulSales(dto.getUnsuccessfulSales());
        } else if (user instanceof Buyer) {
            Buyer b = (Buyer) user;
            if (dto.getCompanyName() != null)
                b.setCompanyName(dto.getCompanyName());
            if (dto.getSuccessfulBuys() != null)
                b.setSuccessfulBuys(dto.getSuccessfulBuys());
            if (dto.getUnsuccessfulBuys() != null)
                b.setUnsuccessfulBuys(dto.getUnsuccessfulBuys());
        } else if (user instanceof LogisticsProvider) {
            LogisticsProvider lp = (LogisticsProvider) user;
            if (dto.getLicenseNumber() != null)
                lp.setLicenseNumber(dto.getLicenseNumber());
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

        Integer latestTrustScore = trustScoreClient.fetchTrustScore(user.getId());
        user.setTrustScore(latestTrustScore);

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
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
        dto.setUsdBalance(user.getUsdBalance());
        dto.setZigBalance(user.getZigBalance());

        switch (user) {
            case Farmer farmer -> {
                dto.setRole("FARMER");
                dto.setFarmName(farmer.getFarmName());
                dto.setFarmLocation(farmer.getFarmLocation());
                dto.setSuccessfulBuys(farmer.getSuccessfulSales());
                dto.setUnsuccessfulSales(farmer.getUnsuccessfulSales());
            }
            case Buyer buyer -> {
                dto.setRole("BUYER");
                dto.setCompanyName(buyer.getCompanyName());
                dto.setSuccessfulBuys(buyer.getSuccessfulBuys());
                dto.setUnsuccessfulBuys(buyer.getUnsuccessfulBuys());
            }
            case LogisticsProvider provider -> {
                dto.setRole("LOGISTICS");
                dto.setLicenseNumber(provider.getLicenseNumber());
            }
            default -> {
            }
        }

        return dto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Map your DB role to Spring Security role
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword()) // already hashed by BCrypt
                .roles(user.getRole().toUpperCase()) // FARMER -> ROLE_FARMER internally
                .build();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
