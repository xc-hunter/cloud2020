package com.xc.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.xc.springcloud.service.PaymentHystrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@DefaultProperties(defaultFallback = "fullHandler")
public class OrderHystrixController {
    @Autowired
    private PaymentHystrixService paymentHystrixService;
    @GetMapping("/consumer/payment/hystrix/ok/{id}")
    public String accessOk(@PathVariable("id")Integer id){
        return paymentHystrixService.accessOk(id);
    };

    @GetMapping("/consumer/payment/hystrix/timeout/{id}")
    @HystrixCommand(fallbackMethod = "paymentTimeOutFallbackMethod",commandProperties ={
            @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="100")
    })
    public String accessTimeout(@PathVariable("id")Integer id){
        return paymentHystrixService.accessTimeout(id);
    };
    public String paymentTimeOutFallbackMethod(@PathVariable("id")Integer id){
        return "我是消费者80的降级服务，对方支付系统繁忙请10s后再试或者自身出现问题，请检查";
    }
    //全局fallback
    public String fullHandler(){
        return "我是全局降级服务";
    }
}
