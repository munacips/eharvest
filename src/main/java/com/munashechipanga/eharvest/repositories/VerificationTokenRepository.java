package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findTopByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<VerificationToken> findByUser_IdAndCodeAndUsedFalse(Long userId, String code);
}
