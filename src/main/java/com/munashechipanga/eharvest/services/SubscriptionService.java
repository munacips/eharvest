package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.SubscriptionDto;
import com.munashechipanga.eharvest.dtos.response.SubscriptionResponseDTO;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponseDTO createSubscription(SubscriptionDto dto);
    SubscriptionResponseDTO updateSubscription(Long id, SubscriptionDto dto);
    SubscriptionResponseDTO getSubscriptionById(Long id);
    void cancelSubscription(Long id);
    void pauseSubscription(Long id);
    void resumeSubscription(Long id);
    List<SubscriptionResponseDTO> getSubscriptionsByBuyer(Long buyerId);
    List<SubscriptionResponseDTO> getSubscriptionsByFarmer(Long farmerId);
    void processSubscription(Long subscriptionId);
}
