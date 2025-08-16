package org.bea.controller;

import lombok.Getter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MainController {

    @GetMapping("/")
    public String redirectToMain() {
        return "forward:/main";
    }

    @GetMapping("/main")
    public String getMain(Model model) {
        model.addAttribute("login", SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
        return "main";
    }

}
