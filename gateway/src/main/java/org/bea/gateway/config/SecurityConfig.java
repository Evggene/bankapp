package org.bea.gateway.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/**").permitAll());
        return http.build();
    }


    @Bean
    FilterRegistrationBean<FormContentFilter> formContentFilterOff() {
        var reg = new FilterRegistrationBean<FormContentFilter>();
        reg.setFilter(new FormContentFilter());
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    FilterRegistrationBean<HiddenHttpMethodFilter> hiddenOff() {
        var reg = new FilterRegistrationBean<HiddenHttpMethodFilter>();
        reg.setFilter(new HiddenHttpMethodFilter());
        reg.setEnabled(false);
        return reg;
    }

}
