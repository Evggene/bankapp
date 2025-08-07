package org.bea.router;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Component
public class RouterFunctions {

    @Bean
    public RouterFunction<ServerResponse> exchageGenerator() {
        return route("exchageGenerator")
                .route(path("/api/rates"), http(URI.create("http://localhost:8010/api/rates")))
                .build();
    }
}
