package com.munashechipanga.eharvest.services.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaynowClient {

    @Value("${paynow.integration.id:}")
    private String integrationId;

    @Value("${paynow.integration.key:}")
    private String integrationKey;

    public PaynowInitResponse initiateDeposit(String email, String phoneNumber, double amount, String currency, String reference) {
        PaynowInitResponse response = new PaynowInitResponse();
        response.setProviderReference("PAYNOW-DEP-" + System.currentTimeMillis());
        response.setRedirectUrl("");
        response.setPollUrl("");
        return response;
    }

    public PaynowInitResponse initiateWithdraw(String email, String phoneNumber, double amount, String currency, String reference) {
        PaynowInitResponse response = new PaynowInitResponse();
        response.setProviderReference("PAYNOW-WD-" + System.currentTimeMillis());
        response.setRedirectUrl("");
        response.setPollUrl("");
        return response;
    }
}
