package org.bea.service;

import lombok.RequiredArgsConstructor;
import org.bea.config.ResilientCall;
import org.bea.config.TransferAppProperties;
import org.bea.domain.TransferOperation;
import org.bea.repo.TransferOperationRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final RestTemplate restTemplate;
    private final TransferOperationRepository repo;
    private final TransferAppProperties props;

    @ResilientCall
    @Transactional
    public UUID transfer(String fromLogin, String toLogin, String fromCurrency, String toCurrency, BigDecimal amount) {
        if (!allowedByBlocker(fromLogin, "TRANSFER", fromCurrency, amount)) {
            var op = baseOp(fromLogin, toLogin, fromCurrency, toCurrency, amount)
                    .status("BLOCKED")
                    .blockerReason("Operation blocked by policy (every 2nd)")
                    .build();
            repo.save(op);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Operation blocked by policy (every 2nd)");
        }

        BigDecimal converted = convert(fromCurrency, toCurrency, amount);

        withdraw(fromLogin, fromCurrency, amount);
        deposit(toLogin, toCurrency, converted);

        var saved = repo.save(
            baseOp(fromLogin, toLogin, fromCurrency, toCurrency, amount)
                .convertedAmount(converted)
                .status("OK")
                .build()
        );
        return saved.getId();
    }

    private boolean allowedByBlocker(String login, String action, String currency, BigDecimal value) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("login", login);
        payload.put("action", action);
        payload.put("currency", currency);
        payload.put("value", value);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var req = new HttpEntity<>(payload, headers);

        String url = props.getGatewayBaseUrl() + "/blocker/check";
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(url, req, Map.class);
        if (resp == null) return false;
        Object allowed = resp.get("allowed");
        return allowed instanceof Boolean && (Boolean) allowed;
    }

    private BigDecimal convert(String from, String to, BigDecimal amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("action", "TRANSFER");
        body.put("from", from);
        body.put("to", to);
        body.put("amount", amount);

        var req = new HttpEntity<>(body, jsonHeaders());
        String url = props.getGatewayBaseUrl() + "/exchange/convert";
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(url, req, Map.class);
        if (resp == null || !resp.containsKey("resultAmount")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Exchange convert failed");
        }
        Object val = resp.get("resultAmount");
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(val.toString());
    }

    private void withdraw(String login, String currency, BigDecimal value) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("currency", currency);
        form.add("value", value.toPlainString());
        form.add("action", "GET"); // снять

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String url = props.getGatewayBaseUrl() + "/cash/user/" + login + "/getCash";
        restTemplate.postForEntity(url, new HttpEntity<>(form, headers), Void.class);
    }

    private void deposit(String login, String currency, BigDecimal value) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("currency", currency);
        form.add("value", value.toPlainString());
        form.add("action", "PUT"); // положить

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String url = props.getGatewayBaseUrl() + "/cash/user/" + login + "/getCash";
        restTemplate.postForEntity(url, new HttpEntity<>(form, headers), Void.class);
    }

    private HttpHeaders jsonHeaders() {
        var h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private TransferOperation.TransferOperationBuilder baseOp(
            String fromLogin, String toLogin, String fromCurrency, String toCurrency, BigDecimal amount
    ) {
        return TransferOperation.builder()
                .id(UUID.randomUUID())
                .fromUser(fromLogin)
                .toUser(toLogin)
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .convertedAmount(BigDecimal.ZERO)
                .ts(OffsetDateTime.now());
    }
}
