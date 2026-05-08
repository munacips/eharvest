package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.configs.VonageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SmsService {

    private final VonageConfig vonageConfig;

    public SmsService(VonageConfig vonageConfig) {
        this.vonageConfig = vonageConfig;
    }

    /**
     * Send SMS message to a phone number using Vonage REST API
     */
    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            String apiKey = vonageConfig.getApiKey();
            String apiSecret = vonageConfig.getApiSecret();
            String from = vonageConfig.getFromNumber();

            String form = "api_key=" + URLEncoder.encode(apiKey == null ? "" : apiKey, StandardCharsets.UTF_8)
                    + "&api_secret=" + URLEncoder.encode(apiSecret == null ? "" : apiSecret, StandardCharsets.UTF_8)
                    + "&to=" + URLEncoder.encode(toPhoneNumber, StandardCharsets.UTF_8)
                    + "&from=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(messageBody, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://rest.nexmo.com/sms/json"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("SMS sent successfully to {}. Response: {}", toPhoneNumber, response.body());
            } else {
                log.error("Failed to send SMS to {}. HTTP {} - {}", toPhoneNumber, response.statusCode(),
                        response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            // Do not throw exception - log and continue
        }
    }
}
