//package org.bea.gateway.config;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.client.*;
//import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
//
//import java.io.IOException;
//import java.util.*;
//
//@Configuration
//public class ClientCredentialsRelayServletFilter {
//
//    private static final String REG_ID = "gateway-client";
//
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE + 20) // раньше фильтров gateway
//    public Filter clientCredentialsHeaderFilter(OAuth2AuthorizedClientManager manager) {
//        return new Filter() {
//            @Override
//            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//                    throws IOException, ServletException {
//
//                HttpServletRequest http = (HttpServletRequest) request;
//                String path = http.getRequestURI();
//
//                // Подставляем токен только для проксируемых вызовов на exchange-generator
//                if (!path.startsWith("/exchange-generator/")) {
//                    chain.doFilter(request, response);
//                    return;
//                }
//
//                var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
//                        .orElseGet(() -> new AnonymousAuthenticationToken(
//                                "gateway", "gateway",
//                                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
//
//                try {
//                    var client = manager.authorize(
//                            OAuth2AuthorizeRequest.withClientRegistrationId(REG_ID).principal(auth).build());
//                    if (client == null || client.getAccessToken() == null) {
//                        chain.doFilter(request, response); // не блокируем; можно вернуть 401, если хочешь
//                        return;
//                    }
//
//                    String token = client.getAccessToken().getTokenValue();
//
//                    // Оборачиваем запрос и добавляем Authorization
//                    HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(http) {
//                        @Override
//                        public String getHeader(String name) {
//                            if ("Authorization".equalsIgnoreCase(name)) {
//                                return "Bearer " + token;
//                            }
//                            return super.getHeader(name);
//                        }
//                        @Override
//                        public Enumeration<String> getHeaders(String name) {
//                            if ("Authorization".equalsIgnoreCase(name)) {
//                                return Collections.enumeration(List.of("Bearer " + token));
//                            }
//                            return super.getHeaders(name);
//                        }
//                        @Override
//                        public Enumeration<String> getHeaderNames() {
//                            List<String> names = Collections.list(super.getHeaderNames());
//                            if (!names.stream().anyMatch(h -> h.equalsIgnoreCase("Authorization"))) {
//                                names.add("Authorization");
//                            }
//                            return Collections.enumeration(names);
//                        }
//                    };
//
//                    chain.doFilter(wrapped, response);
//
//                } catch (OAuth2AuthorizationException ex) {
//                    // при желании можно логировать код ошибки ex.getError().getErrorCode()
//                    chain.doFilter(request, response); // или return 401
//                }
//            }
//        };
//    }
//}
