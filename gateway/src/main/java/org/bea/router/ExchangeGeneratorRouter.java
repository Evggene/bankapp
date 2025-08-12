package org.bea.router;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class ExchangeGeneratorRouter extends RouterCommon {

    public ExchangeGeneratorRouter(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient);
    }

    @Bean
    public RouterFunction<ServerResponse> exchangeGeneratorRoute() {
        return GatewayRouterFunctions.route("exchange-generator-route")
                .route(GatewayRequestPredicates.path("/api/rates/**"), HandlerFunctions.http(getServiceUri("exchange-generator")))
                .build();
    }
}
