package com.xc.springcloud.controller;

import com.xc.springcloud.entities.CommonResult;
import com.xc.springcloud.entities.Payment;
import com.xc.springcloud.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Value("${server.port}")
    private String serverport;
    @PostMapping("/payment/create")
    public CommonResult create(@RequestBody Payment payment){
        int result=paymentService.create(payment);
        if(result>0){
            return new CommonResult(200, "插入数据成功,serverport:"+serverport, result);
        }else{
            return new CommonResult(444,"插入数据失败",null);
        }
    }
    @GetMapping("/payment/get/{id}")
    public CommonResult<Payment> getParamentById(@PathVariable("id") long id){
        Payment payment=paymentService.getPaymentById(id);
        if(payment!=null){
            return new CommonResult(200,"查询成功,serverport:"+serverport,payment);
        }else{
            return new CommonResult(444,"没有对应记录，查询id："+id,null);
        }
    }
    @GetMapping("/payment/lb")
    public  String getLB(){
        return ""+serverport+"被调用";
    }
    @GetMapping("/payment/feign/timeout")
    public String paymentFeignTimeout(){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return serverport;
    }
}
