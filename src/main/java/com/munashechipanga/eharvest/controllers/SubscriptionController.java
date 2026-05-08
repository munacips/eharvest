package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.SubscriptionDto;
import com.munashechipanga.eharvest.dtos.response.SubscriptionResponseDTO;
import com.munashechipanga.eharvest.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/subscriptions")
public class SubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(@RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.createSubscription(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptionsByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByBuyer(buyerId));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> getSubscriptionsByFarmer(@PathVariable Long farmerId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByFarmer(farmerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> updateSubscription(@PathVariable Long id,
                                                                      @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponseDTO> cancelSubscription(@PathVariable Long id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<SubscriptionResponseDTO> pauseSubscription(@PathVariable Long id) {
        subscriptionService.pauseSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponseDTO> resumeSubscription(@PathVariable Long id) {
        subscriptionService.resumeSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<SubscriptionResponseDTO> processSubscription(@PathVariable Long id) {
        subscriptionService.processSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }
}
