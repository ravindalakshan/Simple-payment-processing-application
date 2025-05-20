package com.ebank.paymentservice.exception;

import com.ebank.paymentservice.domain.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException {

    private final String description;
    private final ErrorCode errorCode;

    public PaymentNotFoundException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.description = errorCode.getDefaultMessage();
    }
}
