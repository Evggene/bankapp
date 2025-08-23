package org.bea;

import org.bea.api.dto.ConversionRequest;
import org.bea.api.dto.ConversionResponse;
import org.bea.repo.ConversionOperationRepository;
import org.bea.service.RateClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExchangeControllerIT {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired ConversionOperationRepository repo;
    @MockBean RateClient rateClient;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:ex;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1");
        r.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
        // отключим consul в тестах
        r.add("spring.config.import", () -> "");
    }

    private String url(String path) { return "http://localhost:" + port + path; }

    @Test
    void endToEnd_convert_persistsAndLists() {
        // RUB=1, USD=90, CNY=9 → rate 10, 10 USD → 100 CNY
        given(rateClient.getRatesMap()).willReturn(Map.of(
                "RUB", BigDecimal.ONE,
                "USD", new BigDecimal("90.000000"),
                "CNY", new BigDecimal("9.000000")
        ));

        var req = new ConversionRequest();
        req.setAction("BUY");
        req.setFrom("USD");
        req.setTo("CNY");
        req.setAmount(new BigDecimal("10.00"));

        ConversionResponse resp = rest.postForObject(url("/convert"), req, ConversionResponse.class);
        assertThat(resp).isNotNull();
        assertThat(resp.getResultAmount()).isEqualByComparingTo("100.00");
        assertThat(resp.getConversionRate()).isEqualByComparingTo("10.000000");

        assertThat(repo.findAll()).hasSize(1);
        var list = rest.getForObject(url("/operations"), java.util.List.class);
        assertThat(list).hasSize(1);
    }
}
