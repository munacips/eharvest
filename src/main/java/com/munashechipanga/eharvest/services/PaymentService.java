package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.PaymentRequestDTO;
import com.munashechipanga.eharvest.dtos.response.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO initiatePayment(PaymentRequestDTO dto);
    PaymentResponseDTO handleWebhook(String reference, String status, String providerRef);
}
