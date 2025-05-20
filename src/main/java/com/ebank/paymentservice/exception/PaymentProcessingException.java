package com.ebank.paymentservice.exception;


import com.ebank.paymentservice.domain.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PaymentProcessingException extends RuntimeException {
    private final String description;
    private final ErrorCode errorCode;

    public PaymentProcessingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.description = errorCode.getDefaultMessage();

    }
}
