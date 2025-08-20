package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.domain.CurrencyRate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {

    private final Supplier<String> clientCredentialsToken;
    private final RestTemplate restTemplate;

    @GetMapping("/getRates")
    public Collection<CurrencyRate> getRates() {
        String token = clientCredentialsToken.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List<CurrencyRate>> response = restTemplate.exchange(
                "http://gateway/exchange-generator/getRates",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    @PostMapping("/user/{login}/getCash")
    public void cash(@PathVariable String login,
                       @RequestParam String currency,
                       @RequestParam BigDecimal value,
                       @RequestParam String action) {
        String token = clientCredentialsToken.get();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("currency", currency);
        form.add("value", value.toString());
        form.add("action", action);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        restTemplate.postForEntity("http://gateway/cash/user/" + login + "/getCash",
                new HttpEntity<>(form, headers), Void.class, login);
    }

    @PostMapping("/user/{login}/doTransfer")
    public void doTransfer(@PathVariable String login,
                           @RequestParam("from_currency") String fromCurrency,
                           @RequestParam("to_currency") String toCurrency,
                           @RequestParam("value") java.math.BigDecimal value,
                           @RequestParam("to_login") String toLogin) {
        String token = clientCredentialsToken.get();

        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("from_currency", fromCurrency);
        form.add("to_currency", toCurrency);
        form.add("value", value.toPlainString());
        form.add("to_login", toLogin);

        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        restTemplate.postForEntity("http://gateway/transfer/user/" + login + "/doTransfer",
                new org.springframework.http.HttpEntity<>(form, headers),
                Void.class);
    }
}
