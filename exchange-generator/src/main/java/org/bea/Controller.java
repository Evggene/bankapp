package org.bea;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
public class Controller {

    @GetMapping("/getRates")
    public Collection<CurrencyRate> getRates() {
        return Store.currencyRates.values();
    }
}
