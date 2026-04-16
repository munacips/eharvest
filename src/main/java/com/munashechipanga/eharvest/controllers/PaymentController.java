package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.request.PaymentRequestDTO;
import com.munashechipanga.eharvest.dtos.response.PaymentResponseDTO;
import com.munashechipanga.eharvest.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payments")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/init")
    public ResponseEntity<PaymentResponseDTO> initiate(@RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.initiatePayment(dto));
    }

    @PostMapping("/webhook")
    public ResponseEntity<PaymentResponseDTO> webhook(@RequestParam String reference,
                                                      @RequestParam String status,
                                                      @RequestParam(required = false) String providerRef) {
        return ResponseEntity.ok(paymentService.handleWebhook(reference, status, providerRef));
    }
}
