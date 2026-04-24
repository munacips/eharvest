package com.munashechipanga.eharvest.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TrustScoreClient {

    private static final BigDecimal DEFAULT_TRUST_SCALE = BigDecimal.valueOf(5);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TrustScoreClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${trust-score.api.base-url:http://localhost:8000}") String trustScoreApiBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(trustScoreApiBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    public Integer fetchTrustScore(Long userId) {
        String payload = restClient
                .get()
                .uri("/trust-score/{userId}", userId)
                .retrieve()
                .body(String.class);

        return parseTrustScore(payload);
    }

    private Integer parseTrustScore(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalStateException("Trust score API returned an empty response");
        }

        String trimmed = payload.trim();
        if (trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
            BigDecimal rawScore = new BigDecimal(trimmed);
            return toPercentageScore(rawScore, DEFAULT_TRUST_SCALE);
        }

        try {
            JsonNode root = objectMapper.readTree(trimmed);

            if (root.isNumber()) {
                return toPercentageScore(root.decimalValue(), DEFAULT_TRUST_SCALE);
            }

            if (root.has("trustScore")) {
                BigDecimal rawScore = extractScore(root.get("trustScore"));
                return toPercentageScore(rawScore, extractScale(root));
            }

            if (root.has("trust_score")) {
                BigDecimal rawScore = extractScore(root.get("trust_score"));
                return toPercentageScore(rawScore, extractScale(root));
            }

            if (root.has("score")) {
                BigDecimal rawScore = extractScore(root.get("score"));
                return toPercentageScore(rawScore, extractScale(root));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse trust score API response", ex);
        }

        throw new IllegalStateException("Trust score not found in API response");
    }

    private BigDecimal extractScore(JsonNode scoreNode) {
        if (scoreNode == null || scoreNode.isNull()) {
            throw new IllegalStateException("Trust score field is null");
        }

        if (scoreNode.isNumber()) {
            return scoreNode.decimalValue();
        }

        if (scoreNode.isTextual() && scoreNode.asText().matches("^-?\\d+(\\.\\d+)?$")) {
            return new BigDecimal(scoreNode.asText());
        }

        throw new IllegalStateException("Trust score field is not numeric");
    }

    private BigDecimal extractScale(JsonNode root) {
        JsonNode scaleNode = root.get("scale");
        if (scaleNode == null || scaleNode.isNull()) {
            return DEFAULT_TRUST_SCALE;
        }

        BigDecimal scale;
        if (scaleNode.isNumber()) {
            scale = scaleNode.decimalValue();
        } else if (scaleNode.isTextual() && scaleNode.asText().matches("^-?\\d+(\\.\\d+)?$")) {
            scale = new BigDecimal(scaleNode.asText());
        } else {
            return DEFAULT_TRUST_SCALE;
        }

        return scale.compareTo(BigDecimal.ZERO) > 0 ? scale : DEFAULT_TRUST_SCALE;
    }

    private Integer toPercentageScore(BigDecimal rawScore, BigDecimal scale) {
        BigDecimal percentage = rawScore
                .multiply(HUNDRED)
                .divide(scale, 0, RoundingMode.HALF_UP);
        return percentage.intValue();
    }
}