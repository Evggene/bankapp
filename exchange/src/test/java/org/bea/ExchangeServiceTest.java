package org.bea.service;

import org.bea.api.dto.ConversionRequest;
import org.bea.api.dto.ConversionResponse;
import org.bea.domain.ConversionOperation;
import org.bea.repo.ConversionOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeServiceTest {

    private RateClient rateClient;
    private ConversionOperationRepository repo;
    private ExchangeService service;

    @BeforeEach
    void setUp() {
        rateClient = mock(RateClient.class);
        repo = mock(ConversionOperationRepository.class);
        service = new ExchangeService(rateClient, repo);      // @RequiredArgsConstructor
        when(repo.save(any(ConversionOperation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static ConversionRequest req(String action, String from, String to, String amount) {
        var r = new ConversionRequest();
        r.setAction(action);
        r.setFrom(from);
        r.setTo(to);
        r.setAmount(new BigDecimal(amount));
        return r;
    }

    @Test
    void usdToCny_viaRub_correctMathAndScales() {
        // RUB=1, USD=90, CNY=9 → rate 10.000000 → 10 * 10.00 = 100.00
        when(rateClient.getRatesMap()).thenReturn(Map.of(
                "RUB", BigDecimal.ONE,
                "USD", new BigDecimal("90.000000"),
                "CNY", new BigDecimal("9.000000")
        ));

        ConversionResponse res = service.convert(req("BUY","USD","CNY","10.00"));

        assertThat(res.getFrom()).isEqualTo("USD");
        assertThat(res.getTo()).isEqualTo("CNY");
        assertThat(res.getAmount()).isEqualByComparingTo("10.00");
        assertThat(res.getRateFromRub()).isEqualByComparingTo("90.000000");
        assertThat(res.getRateToRub()).isEqualByComparingTo("9.000000");
        assertThat(res.getConversionRate()).isEqualByComparingTo("10.000000");
        assertThat(res.getResultAmount()).isEqualByComparingTo("100.00");
        assertThat(res.getId()).isNotNull();

        var captor = ArgumentCaptor.forClass(ConversionOperation.class);
        verify(repo).save(captor.capture());
        var op = captor.getValue();
        assertThat(op.getConversionRate()).isEqualByComparingTo("10.000000");
        assertThat(op.getResultAmount()).isEqualByComparingTo("100.00");
        assertThat(op.getTs()).isNotNull();
    }

    @Test
    void rubToUsd_correctMath() {
        when(rateClient.getRatesMap()).thenReturn(Map.of(
                "RUB", BigDecimal.ONE,
                "USD", new BigDecimal("100.000000")
        ));
        var res = service.convert(req("SELL","RUB","USD","2000.00"));
        // 1 / 100 = 0.010000; 2000 * 0.01 = 20.00
        assertThat(res.getConversionRate()).isEqualByComparingTo("0.010000");
        assertThat(res.getResultAmount()).isEqualByComparingTo("20.00");
    }

}
