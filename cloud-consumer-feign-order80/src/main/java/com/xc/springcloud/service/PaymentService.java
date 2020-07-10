package com.xc.springcloud.service;

import com.xc.springcloud.entities.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("CLOUD-PAYMENT-SERVICE")
public interface PaymentService {
    @GetMapping("/payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id")long id);
    @GetMapping("/payment/feign/timeout")
    public String paymentFeignTimeout();
}
