package org.bea.router;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.ProxyExchange;
import org.springframework.context.annotation.Configuration;

import static org.springframework.cloud.gateway.server.mvc.common.MvcUtils.setRequestUrl;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;


@Configuration
public class AccountsRouter extends RouterCommon {
//
    public AccountsRouter(LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient);
    }
//
//    @Bean
//    public RouterFunction<ServerResponse> accountsSignupRoute() {
//        return RouterFunctions.route()
//                .POST("/account/signup", http(getServiceUri("accounts")))
//                .build();
//    }
//
//    @Bean
//    public RouterFunction<ServerResponse> accountLoginRoute() {
//        return RouterFunctions.route()
//                .POST("/account/login", http(getServiceUri("accounts")))
//                .build();
//    }



}
