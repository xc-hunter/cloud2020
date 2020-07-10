package com.xc.springcloud.service;

import org.springframework.stereotype.Component;

@Component
public class PaymentFallbackService implements PaymentHystrixService {
    @Override
    public String accessOk(Integer id) {
        return "PaymentFallbackService------,*****";
    }

    @Override
    public String accessTimeout(Integer id) {
        return "PaymentFallbackService------,*****";
    }
}
