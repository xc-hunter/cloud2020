package com.xc.springcloud.service;

import com.xc.springcloud.dao.PaymentDao;
import com.xc.springcloud.entities.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentDao paymentDao;
    @Override
    public int create(Payment payment){
        return paymentDao.create(payment);
    }
    @Override
    public Payment getPaymentById(long id){
        return paymentDao.getPaymentById(id);
    }

}
