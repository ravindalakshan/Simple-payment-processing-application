package com.ebank.paymentservice.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExternalServiceImpl implements ExternalService {
    @Override
    public void notifyPaymentCreated(UUID paymentId) {
        //Todo: implement
    }

    @Override
    public void notifySwiftPaymentCancelled(UUID paymentId) {
       //Todo: implement
    }
}
