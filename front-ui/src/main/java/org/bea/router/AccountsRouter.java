package org.bea.router;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class AccountsRouter extends RouterCommon {

    public AccountsRouter(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient);
    }

    @Bean
    public RouterFunction<ServerResponse> accountsRoute() {
        return RouterFunctions.route()
                .POST("/account/signup", http(getServiceUri("accounts")))
                .build();
    }
}
