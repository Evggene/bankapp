package org.bea.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bea.domain.SignupRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

@Controller
@RequiredArgsConstructor
public class SecurityController {

    private final Supplier<String> clientCredentialsToken;
    private final RestTemplate restTemplate;

    @GetMapping("/login")
    public String getLogin(@RequestParam(value = "error", required = false) boolean error, Model model) {
        model.addAttribute("error", error);
        return "login";
    }

    @GetMapping("/signup")
    public String getSignup() {
        return "signup";
    }

    @PostMapping("/user/{login}/editPassword")
    public String editPassword(@PathVariable String login,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes ra) {
        String token = clientCredentialsToken.get();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        // ВАЖНО: имена полей как в accounts: EditPasswordRequest { password, confirmPassword, login }
        form.add("password", password);
        form.add("confirmPassword", confirmPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        restTemplate.postForEntity("http://gateway/accounts/user/" + login + "/editPassword",
                new HttpEntity<>(form, headers), Void.class, login);

        ra.addFlashAttribute("message", "Пароль изменён");
        return "redirect:/main";
    }

}
