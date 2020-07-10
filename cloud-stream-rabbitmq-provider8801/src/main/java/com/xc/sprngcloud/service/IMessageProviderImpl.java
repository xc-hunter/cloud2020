package com.xc.sprngcloud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import java.util.UUID;

@EnableBinding(Source.class) //表示当前这个类是source，负责生产消息，并且发送给channel
public class IMessageProviderImpl implements IMessageProvider {
    @Autowired
    private MessageChannel output;  //channel,将信息发送这个channel
    @Override
    public String send() {
        String serial= UUID.randomUUID().toString();
        output.send(MessageBuilder.withPayload(serial).build()); //build方法会创建一个build类
        System.out.println("******serial:"+serial);
        return null;
    }
}
