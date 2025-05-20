package com.ebank.paymentservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PaymentType type;


    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private String debtorIban;
    private String creditorIban;
    private String details;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private BigDecimal cancellationFee;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
