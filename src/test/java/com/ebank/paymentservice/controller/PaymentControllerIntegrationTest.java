package com.ebank.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ebank.paymentservice.domain.Currency;
import com.ebank.paymentservice.domain.Payment;
import com.ebank.paymentservice.domain.PaymentStatus;
import com.ebank.paymentservice.domain.PaymentType;
import com.ebank.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        // Create a test payment
        Payment payment = new Payment();
        payment.setType(PaymentType.SEPA);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency(Currency.EUR);
        payment.setDebtorIban("DE89370400440532013000");
        payment.setCreditorIban("LT121000011101001000");
        payment.setDetails("Test payment");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.ACCEPTED);

        savedPayment = paymentRepository.save(payment);
    }

    @Test
    void createPayment_shouldCreateSEPAPayment_WhenRequestIsValid() throws Exception {
        String paymentJson = """
        {
            "type": "SEPA",
            "amount": 150.75,
            "currency": "EUR",
            "debtorIban": "DE89370400440532013000",
            "creditorIban": "LT121000011101001000",
            "details": "Test SEPA payment"
        }
        """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.paymentType").value("SEPA"))
                .andExpect(jsonPath("$.amount").value(150.75))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void createPayment_shouldCreateSWIFTPayment_WhenRequestIsValid() throws Exception {
        String paymentJson = """
        {
            "type": "SWIFT",
            "amount": 500.00,
            "currency": "USD",
            "debtorIban": "US02999999999999999999",
            "creditorIban": "JP02999999999999999999",
            "details": "Test SWIFT payment"
        }
        """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentType").value("SWIFT"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void createPayment_shouldReturnError_WhenReceiverIbanNotBelongToAcceptedCountries() throws Exception {

        String invalidPaymentJson = """
        {
            "type": "SEPA",
            "amount": 150.75,
            "currency": "EUR",
            "debtorIban": "DE89370400440532013000",
            "creditorIban": "GB29NWBK60161331926819",
            "details": "Invalid SEPA payment"
        }
        """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPaymentJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(containsString("PAYMENT_NOT_ALLOWED")))
                .andExpect(jsonPath("$.description").value(containsString("Payment is not allowed")))
                        .andExpect(jsonPath("$.message").value(equalTo("SEPA payments must be sent to " +
                                "Latvian, Lithuanian or Estonian IBANs")));
    }

    // Cancel payments
    @Test
    void cancelPayment_shouldCancelPayment_WhenPaymentIsNotAlreadyCancelled() throws Exception {
        mockMvc.perform(post("/api/payments/{id}/cancel", savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationFee").exists())
                .andExpect(jsonPath("$.cancelledAt").exists());
    }

    @Test
    void cancelPayment_shouldReturnError_WhenAlreadyCancelled() throws Exception {
        // First cancel
        mockMvc.perform(post("/api/payments/{id}/cancel", savedPayment.getId()));

        // Try to cancel again
        mockMvc.perform(post("/api/payments/{id}/cancel", savedPayment.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(containsString("PAYMENT_ALREADY_CANCELLED")))
                .andExpect(jsonPath("$.message").value(containsString("already cancelled")));
    }

    @Test
    void cancelPayment_shouldReturnError_WhenCancellationPeriodExpired() throws Exception {
        // Create a payment from yesterday
        Payment oldPayment = new Payment();
        oldPayment.setType(PaymentType.SEPA);
        oldPayment.setAmount(new BigDecimal("100.00"));
        oldPayment.setCurrency(Currency.EUR);
        oldPayment.setCreatedAt(LocalDateTime.now().minusDays(1));
        oldPayment.setStatus(PaymentStatus.ACCEPTED);
        Payment savedOldPayment = paymentRepository.save(oldPayment);

        mockMvc.perform(post("/api/payments/{id}/cancel", savedOldPayment.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(containsString("TIME_EXPIRED")))
                .andExpect(jsonPath("$.message").value(containsString("Cancellation allowed only on the day of creation")));
    }

    // Get Payments
    @Test
    void getPayments_shouldReturnAllPayments() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(savedPayment.getId().toString()));
    }

    @Test
    void getPayments_shouldFilterByAmount() throws Exception {
        // Create additional payments for testing
        createTestPayment(new BigDecimal("50.00"));
        createTestPayment(new BigDecimal("200.00"));

        mockMvc.perform(get("/api/payments")
                        .param("minAmount", "100")
                        .param("maxAmount", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    void getPayments_shouldFilterByStatus() throws Exception {
        // Cancel the test payment
        mockMvc.perform(post("/api/payments/{id}/cancel", savedPayment.getId()));

        mockMvc.perform(get("/api/payments")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    void cancelPayment_shouldApplyEarlyFee_WhenCancelledWithin1Hour() throws Exception {
        // Create payment with current timestamp
        Payment payment = createTestPaymentWithCreationTime(LocalDateTime.now());

        mockMvc.perform(post("/api/payments/{id}/cancel", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancellationFee").value(
                        payment.getAmount().multiply(new BigDecimal("0.01")).doubleValue()
                ));
    }

    @Test
    void cancelPayment_shouldApplyLateFee_WhenCancelledAfter1Hour() throws Exception {
        Payment payment = createTestPaymentWithCreationTime(
                LocalDateTime.now().minusHours(2)
        );

        BigDecimal expectedFee = payment.getAmount()
                .multiply(new BigDecimal("0.02"))
                .add(new BigDecimal("0.05")); // Fixed fee

        mockMvc.perform(post("/api/payments/{id}/cancel", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancellationFee").value(expectedFee.doubleValue()));
    }

    @Test
    void cancelPayment_shouldConvertFixedFee_ForUSDCurrency() throws Exception {
        Payment payment = createTestPaymentWithCreationTime(
                LocalDateTime.now().minus(2, ChronoUnit.HOURS)
        );
        payment.setCurrency(Currency.USD);
        payment = paymentRepository.save(payment);

        BigDecimal expectedFee = payment.getAmount()
                .multiply(new BigDecimal("0.02"))
                .add(new BigDecimal("0.05").multiply(new BigDecimal("1.18")));

        mockMvc.perform(post("/api/payments/{id}/cancel", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancellationFee").value(expectedFee.doubleValue()));
    }

    private Payment createTestPaymentWithCreationTime(LocalDateTime createdAt) {
        Payment payment = new Payment();
        payment.setType(PaymentType.SEPA);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency(Currency.EUR);
        payment.setCreatedAt(createdAt);
        payment.setStatus(PaymentStatus.ACCEPTED);
        return paymentRepository.save(payment);
    }

    private void createTestPayment(BigDecimal amount) {
        Payment payment = new Payment();
        payment.setType(PaymentType.SEPA);
        payment.setAmount(amount);
        payment.setCurrency(Currency.EUR);
        payment.setStatus(PaymentStatus.ACCEPTED);
        paymentRepository.save(payment);
    }
}