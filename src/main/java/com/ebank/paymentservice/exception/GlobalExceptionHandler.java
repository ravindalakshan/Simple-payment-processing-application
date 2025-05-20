package com.ebank.paymentservice.exception;

import com.ebank.paymentservice.domain.ErrorCode;
import com.ebank.paymentservice.dto.PaymentErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex
    ) {
        PaymentErrorResponse response = new PaymentErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDescription(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handle PaymentProcessingException
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentProcessing(
            PaymentProcessingException ex
    ) {
        PaymentErrorResponse response = new PaymentErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDescription(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle ValidationException
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<PaymentErrorResponse> handleValidation(
            ValidationException ex
    ) {
        PaymentErrorResponse response = new PaymentErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDescription(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Fallback for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentErrorResponse> handleAllExceptions(Exception ex) {
        PaymentErrorResponse response = new PaymentErrorResponse(
                ErrorCode.SOMETHING_WENT_WRONG,
                "An unexpected error occurred",
                "Please try again.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
