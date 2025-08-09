package org.bea.router;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouterCommon {

    private final LoadBalancerClient loadBalancerClient;

    protected String getServiceUri(String serviceId) {
        return loadBalancerClient.choose(serviceId)
                .getUri()
                .toString();
    }
}
