package org.bea.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalInterceptors((req, body, exec) -> {
                    // Не перетираем существующий Authorization
                    if (!req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        String token = SecurityUtils.currentBearerTokenOrNull();
                        if (token != null && !token.isBlank()) {
                            req.getHeaders().setBearerAuth(token);
                        }
                    }
                    return exec.execute(req, body);
                })
                .build();
    }
}

