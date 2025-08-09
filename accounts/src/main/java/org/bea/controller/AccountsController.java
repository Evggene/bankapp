package org.bea.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountsController {

    @PostMapping("/account/signup")
    public void doSignup() {
        System.out.println();
    }

}
