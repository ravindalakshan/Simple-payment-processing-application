package com.ebank.paymentservice.dto;

import com.ebank.paymentservice.domain.ErrorCode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentErrorResponse {
    private ErrorCode errorCode;
    private String message;
    private String description;
    private LocalDateTime timestamp;

    public PaymentErrorResponse(ErrorCode errorCode, String message, String description, LocalDateTime timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.description = description;
    }
}
