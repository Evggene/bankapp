package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.domain.SignupRequest;
import org.bea.domain.User;
import org.bea.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountsController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public void doSignup(SignupRequest signupRequest) {
        var newUser = User.builder()
                .id(UUID.randomUUID())
                .username(signupRequest.getLogin())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .name(signupRequest.getName())
                .birthdate(signupRequest.getBirthdate())
                .enabled(true)
                .build();
        userRepository.save(newUser);
    }

}
