package org.bea.service;

import lombok.RequiredArgsConstructor;
import org.bea.domain.CashAccount;
import org.bea.domain.dto.CashBalanceResponse;
import org.bea.lib.ResilientCall;
import org.bea.repository.CashAccountRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashAccountRepository repo;
    private final RestTemplate restTemplate;

    @Transactional
    public CashBalanceResponse deposit(String login, String currency, BigDecimal amount) {
        if (!allowedByBlocker(login, "GET", currency, amount)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Operation blocked by policy (every 2nd)");
        }
        CashAccount acc = repo.findByUsernameAndCurrency(login, currency)
                .orElseGet(() -> repo.save(
                        CashAccount.builder()
                                .username(login)
                                .currency(currency)
                                .balance(BigDecimal.ZERO)
                                .build())
                );
        acc.setBalance(acc.getBalance().add(amount));
        repo.save(acc);
        return new CashBalanceResponse(acc.getUsername(), acc.getCurrency(), acc.getBalance(), "Deposited");
    }

    @Transactional
    public CashBalanceResponse withdraw(String login, String currency, BigDecimal amount) {
        if (!allowedByBlocker(login, "GET", currency, amount)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Operation blocked by policy (every 2nd)");
        }
        CashAccount acc = repo.findByUsernameAndCurrency(login, currency)
                .orElseThrow(() -> new IllegalStateException("Счёт не найден"));
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств");
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        repo.save(acc);
        return new CashBalanceResponse(acc.getUsername(), acc.getCurrency(), acc.getBalance(), "Withdrawn");
    }

    @ResilientCall
    private boolean allowedByBlocker(String login, String action, String currency, BigDecimal amount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("login", login);
        payload.put("action", action);
        payload.put("currency", currency);
        payload.put("value", amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

        var decision = restTemplate.postForObject("http://gateway/blocker/check", req, Map.class);
        if (decision == null) return false;
        Object allowed = decision.get("allowed");
        return allowed instanceof Boolean ? (Boolean) allowed : false;
    }
}
