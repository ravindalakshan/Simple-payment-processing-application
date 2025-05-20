package com.ebank.paymentservice.repository;

import com.ebank.paymentservice.domain.Payment;
import com.ebank.paymentservice.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByAmountBetweenAndStatus(BigDecimal minValue, BigDecimal maxValue, PaymentStatus paymentStatus);

    List<Payment> findByAmountBetween(BigDecimal minValue, BigDecimal maxValue);
}
