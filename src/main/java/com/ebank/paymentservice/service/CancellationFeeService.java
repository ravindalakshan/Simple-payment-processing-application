package com.ebank.paymentservice.service;

import com.ebank.paymentservice.domain.Currency;
import com.ebank.paymentservice.domain.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CancellationFeeService implements FeeService {

    private final BigDecimal earlyCancellationFeePercentage;
    private final BigDecimal lateCancellationFeePercentage;
    private final BigDecimal fixedCancellationFeeEur;

    public CancellationFeeService(
            @Value("${cancellation.fee.early.percent:0.01}") BigDecimal earlyCancellationFeePercentage,
            @Value("${cancellation.fee.late.percent:0.02}") BigDecimal lateCancellationFeePercentage,
            @Value("${cancellation.fee.fixed.eur:0.05}") BigDecimal fixedCancellationFeeEur
    ) {
        this.earlyCancellationFeePercentage = earlyCancellationFeePercentage;
        this.lateCancellationFeePercentage = lateCancellationFeePercentage;
        this.fixedCancellationFeeEur = fixedCancellationFeeEur;
    }
    @Override
    public BigDecimal calculateCancellationFee(Payment payment) {

        Duration duration = Duration.between(payment.getCreatedAt(), LocalDateTime.now());
        long minutesSinceCreation = duration.toMinutes();

        BigDecimal feePercentage = minutesSinceCreation > 60 ? lateCancellationFeePercentage : earlyCancellationFeePercentage;
        BigDecimal feeAmount = payment.getAmount().multiply(feePercentage);

        if (minutesSinceCreation > 60) {
            BigDecimal fixedFee = Currency.EUR == payment.getCurrency() ?
                    fixedCancellationFeeEur : convertEurToUsd(fixedCancellationFeeEur);
            feeAmount = feeAmount.add(fixedFee);
        }

        return feeAmount;
    }

    private BigDecimal convertEurToUsd(BigDecimal amountInEur) {
        // In a real application, we would use a currency conversion service
        return amountInEur.multiply(new BigDecimal("1.18"));
    }
}
