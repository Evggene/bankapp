package org.bea.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bea.domain.dto.CashBalanceResponse;
import org.bea.domain.dto.CashFormRequest;
import org.bea.service.CashService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CashController {

    private final CashService svc;
    private final RestTemplate restTemplate;

    @PostMapping(value = "/user/{login}/getCash",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CashBalanceResponse handleForm(@PathVariable String login, @Valid CashFormRequest form) {
        String action = form.getAction();
        if ("PUT".equalsIgnoreCase(action)) {
            notifyOperation("Пополнение: " + login + " +" + form.getValue() + " " + form.getCurrency()
            );
            return svc.deposit(login, form.getCurrency(), form.getValue());
        } else if ("GET".equalsIgnoreCase(action)) {
            notifyOperation("Снятие: " + login + " -" + form.getValue() + " " + form.getCurrency()
            );
            return svc.withdraw(login, form.getCurrency(), form.getValue());
        } else {
            throw new IllegalArgumentException("Неизвестное действие: " + action);
        }
    }

    private void notifyOperation(String message) {
        try {
            // Важно: идём через API шлюза, роут /notifications/** должен быть настроен.
            restTemplate.postForEntity(
                    "http://gateway/notifications/notify?operation={op}",
                    null,
                    Void.class,
                    message
            );
        } catch (Exception ignored) {
        }
    }
}
