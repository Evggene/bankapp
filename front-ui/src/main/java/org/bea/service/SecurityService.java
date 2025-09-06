package org.bea.service;

import lombok.RequiredArgsConstructor;
import org.bea.config.SharedAppProperties;
import org.bea.lib.ResilientCall;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SecurityService implements UserDetailsService {

    private final RestTemplate restTemplate;
    private final SharedAppProperties properties;

    @ResilientCall
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var user = restTemplate.getForObject(
                        "/accounts/loadUser?user=" +
                        username,
                org.bea.domain.User.class);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .roles("USER")
                .build();
    }
}
