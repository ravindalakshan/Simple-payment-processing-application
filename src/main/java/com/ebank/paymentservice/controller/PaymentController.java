package com.ebank.paymentservice.controller;

import com.ebank.paymentservice.dto.CreatePaymentRequest;
import com.ebank.paymentservice.dto.CreatePaymentResponse;
import com.ebank.paymentservice.service.CountryResolverService;
import com.ebank.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final CountryResolverService countryResolverService;

    public PaymentController(PaymentService paymentService, CountryResolverService countryResolverService) {
        this.paymentService = paymentService;
        this.countryResolverService = countryResolverService;
    }

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        countryResolverService.logRequestCountry();
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CreatePaymentResponse> cancelPayment(@PathVariable UUID id) {
        countryResolverService.logRequestCountry();
        return ResponseEntity.ok(paymentService.cancelPayment(id));
    }

    @GetMapping
    public ResponseEntity<List<CreatePaymentResponse>> getPayments(
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String status) {
        countryResolverService.logRequestCountry();
        return ResponseEntity.ok(paymentService.getPayments(minAmount, maxAmount, status));
    }
}
