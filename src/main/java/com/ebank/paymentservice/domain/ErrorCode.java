package com.ebank.paymentservice.domain;

public enum ErrorCode {
    // Payment Errors (P_ prefix)
    PAYMENT_NOT_FOUND("Payment not found"),
    PAYMENT_ALREADY_CANCELLED("Payment already cancelled"),
    TIME_EXPIRED("Cancellation time expired"),
    INVALID_IBAN("Invalid IBAN format"),
    INCORRECT_CURRENCY("SEPA payments require EUR currency"),

    PAYMENT_NOT_ALLOWED("Payment is not allowed"),

    // Validation Errors (V_ prefix)
    INVALID_REQUEST("Invalid request data"),

    // System Errors (S_ prefix)
    SOMETHING_WENT_WRONG("Unknown error occurred "),
    SERVICE_UNAVAILABLE("Service unavailable");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
