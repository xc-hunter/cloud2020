package com.xc.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MyLb implements LoadBalance {
    private AtomicInteger atomicInteger=new AtomicInteger(0);
    public final int getAndIncrement(){
        int current;
        int next;
        do{
            current=this.atomicInteger.get();
            next=current>=2147483647?0:current+1;
        }while(!this.atomicInteger.compareAndSet(current, next));
        return next;
    }
    @Override
    public ServiceInstance returnInstance(List<ServiceInstance> serviceInstanceList) {

        int index=this.getAndIncrement()%serviceInstanceList.size();
        return serviceInstanceList.get(index);
    }
}
