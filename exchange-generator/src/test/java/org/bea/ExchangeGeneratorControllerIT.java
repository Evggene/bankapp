package org.bea;

import org.bea.domain.CurrencyRate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExchangeGeneratorControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.config.import", () -> "");
    }

    @Test
    void getRates_returnsAtLeastThreeIncludingRubUsdCny() {
        var response = rest.getForObject("http://localhost:" + port + "/getRates", CurrencyRate[].class);
        assertThat(response).isNotNull();
        assertThat(response.length).isGreaterThanOrEqualTo(3);
        assertThat(java.util.Arrays.stream(response).map(CurrencyRate::getName))
                .contains("RUB","USD","CNY");
    }
}
