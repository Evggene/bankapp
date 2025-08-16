package org.bea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.List;

@Configuration
public class OAuth2ClientManagerConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientService clientService
    ) {
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    /** Удобный метод: получить Bearer-токен по registrationId = "gateway-client" */
    @Bean
    public java.util.function.Supplier<String> clientCredentialsTokenSupplier(OAuth2AuthorizedClientManager manager) {
        return () -> {
            var principal = new AnonymousAuthenticationToken(
                    "front-ui", "front-ui", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            var req = OAuth2AuthorizeRequest.withClientRegistrationId("front_ui_client")
                    .principal(principal)
                    .build();
            var client = manager.authorize(req);
            if (client == null || client.getAccessToken() == null) {
                throw new IllegalStateException("Не удалось получить client_credentials токен");
            }
            return client.getAccessToken().getTokenValue();
        };
    }
}
