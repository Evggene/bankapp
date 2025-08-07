package org.bea.controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("/")
public class MainController {

    @GetMapping
    public String redirectToMain() {
        return "main";
    }

    @GetMapping("main")
    public String getMain() {
        return "main";
    }

}
