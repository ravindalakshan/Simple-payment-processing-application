package com.ebank.paymentservice.service;

import com.ebank.paymentservice.dto.CreatePaymentRequest;
import com.ebank.paymentservice.dto.CreatePaymentResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    CreatePaymentResponse createPayment(CreatePaymentRequest request);
    CreatePaymentResponse cancelPayment(UUID id);
    List<CreatePaymentResponse> getPayments(BigDecimal minAmount, BigDecimal maxAmount, String status);
}
