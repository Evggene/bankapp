package org.bea.controller;

import org.bea.domain.SignupRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountsController {

    @PostMapping("/account/signup")
    public void doSignup(SignupRequest signupRequest) {
        System.out.println();
    }

}
