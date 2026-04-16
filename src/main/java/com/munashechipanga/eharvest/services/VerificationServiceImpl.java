package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.entities.VerificationToken;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.UserRepository;
import com.munashechipanga.eharvest.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationServiceImpl implements VerificationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    NotificationService notificationService;

    @Override
    public void requestVerification(Long userId, String channel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String code = generateCode();

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setCode(code);
        token.setChannel(channel != null ? channel : "EMAIL");
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationTokenRepository.save(token);

        notificationService.sendVerification(user, "Verification code", "Your verification code is: " + code);
    }

    @Override
    public void confirmVerification(Long userId, String code) {
        VerificationToken token = verificationTokenRepository.findByUser_IdAndCodeAndUsedFalse(userId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification code"));
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }
        token.setUsed(true);
        verificationTokenRepository.save(token);

        User user = token.getUser();
        user.setVerified(true);
        userRepository.save(user);
    }

    private String generateCode() {
        int code = new Random().nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
