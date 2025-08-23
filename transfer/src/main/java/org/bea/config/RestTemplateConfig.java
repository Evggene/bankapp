package org.bea.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((req, body, exec) -> {
            // не перетираем уже установленный Authorization, если он есть
            if (!req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                String token = SecurityUtils.currentBearerTokenOrNull();
                if (token != null) {
                    req.getHeaders().setBearerAuth(token);
                }
            }
            return exec.execute(req, body);
        });
        return rt;
    }
}
