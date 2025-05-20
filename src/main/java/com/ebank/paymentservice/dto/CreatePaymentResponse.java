package com.ebank.paymentservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreatePaymentResponse {
    private UUID id;
    private BigDecimal amount;
    private String paymentType;
    private String currency;
    private String debtorIban;
    private String creditorIban;
    private String details;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private BigDecimal cancellationFee;
    private String status;
}
