package org.bea.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bea.domain.EditPasswordRequest;
import org.bea.domain.EditUserAccountsRequest;
import org.bea.domain.SignupRequest;
import org.bea.metrics.AccountsMetrics;
import org.bea.repository.User;
import org.bea.repository.UserRepository;
import org.bea.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountsController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AccountsMetrics accountsMetrics;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

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

    @GetMapping("/loadUser")
    public User loadUser(@RequestParam(value = "user", required = false) String user) {
        log.info("load user: {}", user);
        return userRepository.findByUsername(user)
                .map(u -> {
                    accountsMetrics.recordSignup(true);
                    return u;
                })
                .orElseThrow(() -> {
                    accountsMetrics.recordSignup(false);
                    return new UsernameNotFoundException("User not found with username: " + user);
                });
    }

    @PostMapping("/user/{login}/editPassword")
    public void editPassword(@PathVariable(value = "login", required = false) String login,
                  @ModelAttribute EditPasswordRequest editPasswordRequest) {
        editPasswordRequest.setLogin(login);
        userService.updatePassword(login, editPasswordRequest.getPassword());
    }

    @PostMapping("/user/{login}/editUserAccounts")
    public void editUserAccounts(@PathVariable(value = "login", required = false) String login,
                             @ModelAttribute EditUserAccountsRequest editUserAccountsRequest) {
        userService.editUserAccount(login, editUserAccountsRequest.getName(), editUserAccountsRequest.getBirthdate());
    }

}
