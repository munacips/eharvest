package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.entities.Subscription;
import com.munashechipanga.eharvest.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubscriptionScheduler {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 6 * * *")
    public void processDueSubscriptions() {
        List<Subscription> due = subscriptionRepository
                .findByStatusAndNextDeliveryDateBefore("ACTIVE", LocalDateTime.now());
        for (Subscription subscription : due) {
            subscriptionService.processSubscription(subscription.getId());
        }
    }
}
