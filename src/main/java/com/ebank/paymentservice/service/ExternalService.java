package com.ebank.paymentservice.service;

import java.util.UUID;

public interface ExternalService {
    void notifyPaymentCreated(UUID paymentId);
    void notifySwiftPaymentCancelled(UUID paymentId);
}
