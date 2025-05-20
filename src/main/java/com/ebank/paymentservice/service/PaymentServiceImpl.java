package com.ebank.paymentservice.service;

import com.ebank.paymentservice.domain.*;
import com.ebank.paymentservice.exception.PaymentNotFoundException;
import com.ebank.paymentservice.exception.PaymentProcessingException;
import com.ebank.paymentservice.exception.ValidationException;
import com.ebank.paymentservice.domain.*;
import com.ebank.paymentservice.dto.CreatePaymentRequest;
import com.ebank.paymentservice.dto.CreatePaymentResponse;
import com.ebank.paymentservice.exception.*;
import com.ebank.paymentservice.repository.PaymentRepository;
import com.ebank.paymentservice.util.IbanValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final ExternalService externalService;
    private final FeeService feeService;
    private final IbanValidator ibanValidator;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ExternalService externalService,
                              IbanValidator ibanValidator,
                              FeeService feeService) {

        this.paymentRepository = paymentRepository;
        this.externalService = externalService;
        this.ibanValidator = ibanValidator;
        this.feeService = feeService;
    }


    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {

        validatePaymentRequest(request);

        Payment payment = new Payment();
        payment.setType(request.getType());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setDebtorIban(request.getDebtorIban());
        payment.setCreditorIban(request.getCreditorIban());
        payment.setDetails(request.getDetails());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.ACCEPTED);

        Payment savedPayment = paymentRepository.save(payment);

        // Notify external service
        externalService.notifyPaymentCreated(savedPayment.getId());

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional
    public CreatePaymentResponse cancelPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new PaymentProcessingException(ErrorCode.PAYMENT_ALREADY_CANCELLED, "Payment "+ id +" already cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdDate = payment.getCreatedAt();

        if (now.toLocalDate().isAfter(createdDate.toLocalDate())) {
            throw new PaymentProcessingException(ErrorCode.TIME_EXPIRED, "Cancellation allowed only on the day of " +
                    "creation");
        }

        BigDecimal cancellationFee = feeService.calculateCancellationFee(payment);

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancellationFee(cancellationFee);
        payment.setCancelledAt(now);

        Payment cancelledPayment = paymentRepository.save(payment);

        if (cancelledPayment.getType() == PaymentType.SWIFT) {
            externalService.notifySwiftPaymentCancelled(cancelledPayment.getId());
        }

        return mapToResponse(cancelledPayment);
    }

    @Override
    public List<CreatePaymentResponse> getPayments(BigDecimal minAmount, BigDecimal maxAmount, String status) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;

        if (minAmount == null && maxAmount == null && status == null) {
            return paymentRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        BigDecimal maxValue = maxAmount != null ? maxAmount : BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal minValue = minAmount != null ? minAmount : BigDecimal.ZERO;

        if (minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("minAmount cannot be greater than maxAmount");
        }

        if (paymentStatus != null) {
            return paymentRepository.findByAmountBetweenAndStatus(
                    minValue,
                    maxValue,
                    paymentStatus
            ).stream().map(this::mapToResponse).collect(Collectors.toList());
        } else {
            return paymentRepository.findByAmountBetween(
                    minValue,
                    maxValue
            ).stream().map(this::mapToResponse).collect(Collectors.toList());
        }
    }

    private void validatePaymentRequest(CreatePaymentRequest request) {
        if (ibanValidator.isInvalid(request.getDebtorIban())) {
            throw new ValidationException(ErrorCode.INVALID_IBAN, "debtor/sender IBAN is not valid");
        }

        if (ibanValidator.isInvalid(request.getCreditorIban())) {
            throw new ValidationException(ErrorCode.INVALID_IBAN, "creditor/receiver IBAN is not valid");
        }

        if (request.getType() == PaymentType.SEPA) {
            if (request.getCurrency() != Currency.EUR) {
                throw new ValidationException(ErrorCode.INCORRECT_CURRENCY, "SEPA payments must be in EUR");
            }

            if (!ibanValidator.isSEPACountryIban(request.getCreditorIban())) {
                throw new ValidationException(ErrorCode.PAYMENT_NOT_ALLOWED, "SEPA payments must be sent to Latvian, " +
                        "Lithuanian or Estonian IBANs");
            }
        }
    }

    private CreatePaymentResponse mapToResponse(Payment payment) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setId(payment.getId());
        response.setPaymentType(payment.getType().name());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency().toString());
        response.setDebtorIban(payment.getDebtorIban());
        response.setCreditorIban(payment.getCreditorIban());
        response.setDetails(payment.getDetails());
        response.setCreatedAt(payment.getCreatedAt());
        response.setStatus(payment.getStatus().name());
        response.setCancellationFee(payment.getCancellationFee());
        response.setCancelledAt(payment.getCancelledAt());
        return response;
    }
}
