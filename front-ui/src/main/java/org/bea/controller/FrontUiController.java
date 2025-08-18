package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.domain.CurrencyRate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Controller
@RequiredArgsConstructor
public class FrontUiController {

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

    @PostMapping("/user/{login}/editUserAccounts")
    public String editUserAccounts(@PathVariable String login,
                               @RequestParam String name,
                               @RequestParam String birthdate) {
        String token = clientCredentialsToken.get();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", name);
        form.add("birthdate", birthdate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        restTemplate.postForEntity("http://gateway/accounts/user/" + login + "/editUserAccounts",
                new HttpEntity<>(form, headers), Void.class, login);

        return "redirect:/main";
    }

}
