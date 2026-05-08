package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.request.PaymentRequestDTO;
import com.munashechipanga.eharvest.dtos.response.PaymentResponseDTO;
import com.munashechipanga.eharvest.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<PaymentResponseDTO> webhook(@RequestParam Map<String, String> fields) {
        return ResponseEntity.ok(paymentService.handleWebhook(fields));
    }

    @GetMapping("/return")
    public ResponseEntity<PaymentResponseDTO> paymentReturn(@RequestParam String reference) {
        return ResponseEntity.ok(paymentService.handleWebhook(reference, null, null));
    }
}
