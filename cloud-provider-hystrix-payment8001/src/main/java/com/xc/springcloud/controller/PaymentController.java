package com.xc.springcloud.controller;

import com.xc.springcloud.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Value("${server.port}")
    private String serverport;
    @GetMapping("/payment/hystrix/ok/{id}")
    public String accessOk(@PathVariable("id")Integer id){
        return paymentService.paymentInfo_OK(id);
    }
    @GetMapping("/payment/hystrix/timeout/{id}")
    public String accessTimeout(@PathVariable("id")Integer id){
        return paymentService.paymentInfo_Timeout(id);
    }
    //===服务熔断
    @GetMapping("/payment/circuit/{id}")
    public String paymentCircuitBreaker(@PathVariable("id")Integer id){
        return paymentService.paymentCircuitBreaker(id);
    }
}
