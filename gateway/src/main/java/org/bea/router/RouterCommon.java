package org.bea.router;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Configuration;

import javax.naming.ServiceUnavailableException;

@Configuration
@RequiredArgsConstructor
public class RouterCommon {

    private final LoadBalancerClient loadBalancerClient;

    protected String getServiceUri(String serviceId) {
        ServiceInstance instance = loadBalancerClient.choose(serviceId);
        if (instance == null) {
            return "localhost:8080";
        }
        return instance.getUri().toString();
    }
}
