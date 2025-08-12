package org.bea.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.bea.domain.SignupRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SecurityController {

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSignup() {
        return "signup";
    }

    @PostMapping(value = "/account/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void signupDiag(@ModelAttribute SignupRequest signupRequest) {
        System.out.println();
    }
}
