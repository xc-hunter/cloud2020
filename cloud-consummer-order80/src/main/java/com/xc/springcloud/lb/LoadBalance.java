package com.xc.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface LoadBalance {
    ServiceInstance returnInstance(List<ServiceInstance> serviceInstanceList);
}
