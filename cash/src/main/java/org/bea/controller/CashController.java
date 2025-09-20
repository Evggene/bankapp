package org.bea.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bea.domain.dto.CashBalanceResponse;
import org.bea.domain.dto.CashFormRequest;
import org.bea.dto.NotificationDto;
import org.bea.service.CashService;
import org.bea.service.NotificationProducer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CashController {

    private final CashService svc;
    private final NotificationProducer notificationProducer;

    @PostMapping(value = "/user/{login}/getCash",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CashBalanceResponse handleForm(@PathVariable String login, @Valid CashFormRequest form) {
        var action = form.getAction();
        if ("PUT".equalsIgnoreCase(action)) {
            return handle("Пополнение", login, form, svc.deposit(login, form.getCurrency(), form.getValue()));
        } else if ("GET".equalsIgnoreCase(action)) {
            return handle("Снятие", login, form, svc.withdraw(login, form.getCurrency(), form.getValue()));
        } else {
            throw new IllegalArgumentException("Неизвестное действие: " + action);
        }
    }

    private CashBalanceResponse handle(String op, String login, CashFormRequest form, CashBalanceResponse svc) {
        var notification = new NotificationDto(op, login, form.getValue().toString(), form.getCurrency());
        notificationProducer.sendMessage(notification);
        return svc;
    }
}
