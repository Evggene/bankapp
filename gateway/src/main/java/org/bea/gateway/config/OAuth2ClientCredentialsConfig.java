//package org.bea.gateway.config;
//
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.*;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
//import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
//import org.springframework.security.oauth2.core.AuthorizationGrantType;
//import org.springframework.util.Assert;
//
//@Configuration
//public class OAuth2ClientCredentialsConfig {
//
//    @Bean
//    OAuth2AuthorizedClientManager authorizedClientManager(
//            ClientRegistrationRepository registrations,
//            OAuth2AuthorizedClientService clientService) {
//
//        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
//                .clientCredentials()
//                .build();
//
//        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
//                registrations, clientService);
//        manager.setAuthorizedClientProvider(provider);
//        return manager;
//    }
//
//}
