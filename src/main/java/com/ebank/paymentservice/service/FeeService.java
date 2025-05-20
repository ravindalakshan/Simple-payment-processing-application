package com.ebank.paymentservice.service;

import com.ebank.paymentservice.domain.Payment;

import java.math.BigDecimal;

public interface FeeService {
    BigDecimal calculateCancellationFee(Payment payment);
}
