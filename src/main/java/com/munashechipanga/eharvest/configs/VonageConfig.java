package com.munashechipanga.eharvest.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class VonageConfig {

    @Value("${vonage.api-key:}")
    private String apiKey;

    @Value("${vonage.api-secret:rSASwX6Xu0KnPiRC}")
    private String apiSecret;

    @Value("${vonage.from-number:+1234567890}")
    private String fromNumber;

    @PostConstruct
    public void initialize() {
        log.info("Vonage configuration loaded. From number: {}", fromNumber);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getFromNumber() {
        return fromNumber;
    }
}
