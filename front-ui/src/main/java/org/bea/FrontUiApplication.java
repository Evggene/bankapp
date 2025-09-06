package org.bea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.client.RestTemplate;

@EnableWebSecurity
@SpringBootApplication
public class FrontUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontUiApplication.class, args);
    }

}
