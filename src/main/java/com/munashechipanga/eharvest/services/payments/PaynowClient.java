package com.munashechipanga.eharvest.services.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaynowClient {
    private static final String INITIATE_URL = "https://www.paynow.co.zw/interface/initiatetransaction";
    private static final DecimalFormat AMOUNT_FORMAT =
            new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private static final Logger logger = LoggerFactory.getLogger(PaynowClient.class);

    @Value("${paynow.integration.id:}")
    private String integrationId;

    @Value("${paynow.integration.key:}")
    private String integrationKey;

    @Value("${paynow.return-url:http://localhost:8080/api/v1/payments/return?reference={reference}}")
    private String returnUrl;

    @Value("${paynow.result-url:http://localhost:8080/api/v1/payments/webhook}")
    private String resultUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaynowInitResponse initiateDeposit(String email, String phoneNumber, double amount, String currency, String reference) {
        requireConfigured();

        Map<String, String> request = new LinkedHashMap<>();
        request.put("id", integrationId);
        request.put("reference", reference);
        request.put("amount", AMOUNT_FORMAT.format(amount));
        request.put("additionalinfo", "eHarvest wallet deposit " + reference);
        request.put("returnurl", buildReturnUrl(reference));
        request.put("resulturl", resultUrl);
        if (email != null && !email.isBlank()) {
            request.put("authemail", email);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            request.put("authphone", phoneNumber);
        }
        request.put("status", "Message");
        request.put("hash", generateHash(request));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        request.forEach(form::add);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String rawResponse = restTemplate.postForObject(INITIATE_URL, new HttpEntity<>(form, headers), String.class);
        Map<String, String> parsed = parseResponseFields(rawResponse);
        logger.warn("Paynow raw response snippet: {}", snippet(rawResponse));
        logger.warn("Paynow parsed response fields: {}", parsed);
        String computedHash = generateHash(parsed);
        logger.warn("Paynow supplied hash: {}, computed hash: {}", value(parsed, "hash"), computedHash);

        String suppliedHash = value(parsed, "hash");
        if (suppliedHash == null || suppliedHash.isBlank() || !suppliedHash.equalsIgnoreCase(computedHash)) {
            throw new IllegalStateException("Invalid Paynow response hash. Raw response snippet: " + snippet(rawResponse));
        }

        PaynowInitResponse response = new PaynowInitResponse();
        response.setStatus(value(parsed, "status"));
        response.setError(value(parsed, "error"));
        if (!"Ok".equalsIgnoreCase(response.getStatus())) {
            throw new IllegalStateException("Paynow initiation failed: " + response.getError());
        }
        response.setProviderReference(value(parsed, "paynowreference"));
        response.setRedirectUrl(value(parsed, "browserurl"));
        response.setPollUrl(value(parsed, "pollurl"));
        return response;
    }

    public PaynowInitResponse initiateWithdraw(String email, String phoneNumber, double amount, String currency, String reference) {
        PaynowInitResponse response = new PaynowInitResponse();
        response.setProviderReference("PAYOUT-" + reference);
        response.setStatus("PENDING_PAYOUT");
        response.setRedirectUrl("");
        response.setPollUrl("");
        return response;
    }

    public boolean isHashValid(Map<String, String> fields) {
        String supplied = value(fields, "hash");
        if (supplied == null || supplied.isBlank()) {
            return false;
        }
        return supplied.equalsIgnoreCase(generateHash(fields));
    }

    public Map<String, String> parseFormEncoded(String body) {
        Map<String, String> values = new LinkedHashMap<>();
        if (body == null || body.isBlank()) {
            return values;
        }
        for (String pair : body.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            values.put(
                    URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8)
            );
        }
        return values;
    }

    public Map<String, String> parseResponseFields(String body) {
        if (body == null || body.isBlank()) {
            return new LinkedHashMap<>();
        }

        String trimmed = body.trim();
        if (looksLikeHtml(trimmed)) {
            Map<String, String> htmlFields = parseHtmlInputs(trimmed);
            if (!htmlFields.isEmpty()) {
                return htmlFields;
            }
        }

        Map<String, String> formFields = parseFormEncoded(trimmed);
        if (!formFields.isEmpty()) {
            return formFields;
        }

        return new LinkedHashMap<>();
    }

    private String buildReturnUrl(String reference) {
        return returnUrl.replace("{reference}", reference);
    }

    private boolean looksLikeHtml(String body) {
        String lower = body.toLowerCase(Locale.ROOT);
        return lower.startsWith("<") || lower.contains("<html") || lower.contains("<form") || lower.contains("<input");
    }

    private Map<String, String> parseHtmlInputs(String body) {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher inputMatcher = Pattern.compile("(?is)<input\\b([^>]*?)>").matcher(body);
        while (inputMatcher.find()) {
            String attributes = inputMatcher.group(1);
            String name = extractAttribute(attributes, "name");
            if (name == null || name.isBlank()) {
                continue;
            }
            String value = extractAttribute(attributes, "value");
            values.put(name, unescapeHtml(value != null ? value : ""));
        }
        return values;
    }

    private String extractAttribute(String attributes, String attributeName) {
        Matcher quoted = Pattern.compile("(?is)\\b" + Pattern.quote(attributeName) + "\\s*=\\s*(['\"])(.*?)\\1").matcher(attributes);
        if (quoted.find()) {
            return quoted.group(2);
        }
        Matcher unquoted = Pattern.compile("(?is)\\b" + Pattern.quote(attributeName) + "\\s*=\\s*([^\\s>]+)").matcher(attributes);
        if (unquoted.find()) {
            return unquoted.group(1);
        }
        return null;
    }

    private String unescapeHtml(String value) {
        return value
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    private String snippet(String value) {
        if (value == null) {
            return "<null>";
        }
        String trimmed = value.replaceAll("\\s+", " ").trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500) + "...";
    }

    private void requireConfigured() {
        if (integrationId == null || integrationId.isBlank() || integrationKey == null || integrationKey.isBlank()) {
            throw new IllegalStateException("Paynow integration id and key must be configured");
        }
    }

    private String generateHash(Map<String, String> values) {
        // Preserve the exact field order received from Paynow.
        StringBuilder raw = new StringBuilder();
        values.forEach((key, value) -> {
            if (!"hash".equalsIgnoreCase(key)) {
                raw.append(value != null ? value.trim() : "");
            }
        });
        raw.append(integrationKey);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] bytes = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 is not available", e);
        }
    }

    private String value(Map<String, String> values, String key) {
        return values.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
