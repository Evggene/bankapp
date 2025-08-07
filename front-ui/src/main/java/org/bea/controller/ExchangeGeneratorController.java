package org.bea.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ExchangeGeneratorController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/api/rates")
    public Object t() {
        return restTemplate.getForObject("http://exchange-generator/api/rates", String.class);
    }
}
