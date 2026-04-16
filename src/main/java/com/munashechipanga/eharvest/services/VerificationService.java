package com.munashechipanga.eharvest.services;

public interface VerificationService {
    void requestVerification(Long userId, String channel);
    void confirmVerification(Long userId, String code);
}
