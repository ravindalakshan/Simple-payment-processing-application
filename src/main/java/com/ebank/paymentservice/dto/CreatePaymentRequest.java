package com.ebank.paymentservice.dto;

import com.ebank.paymentservice.domain.Currency;
import com.ebank.paymentservice.domain.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    @NotNull
    private PaymentType type;

    @Positive
    @NotNull
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotNull
    private String debtorIban;

    @NotNull
    private String creditorIban;

    @Size(max = 200)
    private String details;
}
