package org.bea;

import org.bea.config.SecurityUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class TransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
    }

    @Bean
    @LoadBalanced  // Делает RestTemplate "discovery-aware"
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

