package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsProviderDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.LogisticsProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogisticsProviderServiceImpl implements LogisticsProviderService {

    @Autowired
    LogisticsProviderRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TrustScoreClient trustScoreClient;

    @Override
    public UserResponseDTO getLogisticsProviderById(Long id) {
        LogisticsProvider provider = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        Integer latestTrustScore = trustScoreClient.fetchTrustScore(provider.getId());
        provider.setTrustScore(latestTrustScore);

        LogisticsProvider updatedProvider = repository.save(provider);
        return mapToResponse(updatedProvider);
    }

    @Override
    public UserResponseDTO createLogisticsProvider(LogisticsProviderDto dto) {

        LogisticsProvider provider = new LogisticsProvider();

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String password = passwordEncoder.encode(dto.getPassword());

        provider.setFirstName(dto.getFirstName());
        provider.setLastName(dto.getLastName());
        provider.setAddress(dto.getAddress());
        provider.setEmail(dto.getEmail());
        provider.setUsername(dto.getUsername());
        provider.setNationalId(dto.getNationalId());
        provider.setRole("LOGISTICS");
        provider.setEmail(dto.getEmail());
        provider.setPassword(password);
        provider.setPhoneNumber(dto.getPhoneNumber());
        provider.setAddress(dto.getAddress());
        provider.setActive(dto.getActive());
        provider.setVerified(dto.getVerified());
        provider.setTrustScore(dto.getTrustScore());
        provider.setLicenseNumber(dto.getLicenseNumber());
        provider.setDefensiveId(dto.getDefensiveId());

        LogisticsProvider newProvider = repository.save(provider);

        return mapToResponse(newProvider);

    }

    @Override
    public UserResponseDTO updateLogisticsProvider(Long id, LogisticsProviderDto dto) {
        LogisticsProvider provider = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        if (dto.getFirstName() != null)
            provider.setFirstName(dto.getFirstName());
        if (dto.getPhoneNumber() != null)
            provider.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getActive() != null)
            provider.setActive(dto.getActive());
        if (dto.getVerified() != null)
            provider.setVerified(dto.getVerified());
        if (dto.getTrustScore() != null)
            provider.setTrustScore(dto.getTrustScore());
        if (dto.getDefensiveId() != null)
            provider.setDefensiveId(dto.getDefensiveId());
        if (dto.getLicenseNumber() != null)
            provider.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getLastName() != null)
            provider.setLastName(dto.getLastName());
        if (dto.getAddress() != null)
            provider.setAddress(dto.getAddress());
        if (dto.getEmail() != null)
            provider.setEmail(dto.getEmail());
        if (dto.getUsername() != null)
            provider.setUsername(dto.getUsername());
        if (dto.getNationalId() != null)
            provider.setNationalId(dto.getNationalId());
        if (dto.getRole() != null)
            provider.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            provider.setPassword(encodedPassword);
        }

        return mapToResponse(repository.save(provider));
    }

    @Override
    public void deleteLogisticsProvider(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<UserResponseDTO> getAllLogisticsProviders() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserResponseDTO mapToResponse(LogisticsProvider user) {
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
        dto.setRole("LOGISTICS");
        dto.setLicenseNumber(user.getLicenseNumber());
        dto.setDefensiveId(user.getDefensiveId());
        return dto;
    }
}
