package com.xc.springcloud.service;

import com.xc.springcloud.entities.Payment;

public interface PaymentService {
     int create(Payment payment);
     Payment getPaymentById(long id);
}
