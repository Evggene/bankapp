package org.bea;

import org.bea.api.ExchangeController;
import org.bea.api.dto.ConversionRequest;
import org.bea.api.dto.ConversionResponse;
import org.bea.config.SecurityConfig;
import org.bea.domain.ConversionOperation;
import org.bea.repo.ConversionOperationRepository;
import org.bea.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExchangeController.class)
@Import(SecurityConfig.class)
class ExchangeControllerWebMvcTest {

    @Autowired MockMvc mockMvc;
    @MockBean ExchangeService exchangeService;
    @MockBean ConversionOperationRepository repo;

    @Test
    void post_convert_returnsResponseFromService() throws Exception {
        var resp = ConversionResponse.builder()
                .id(UUID.randomUUID())
                .action("BUY")
                .from("USD")
                .to("CNY")
                .amount(new BigDecimal("10.00"))
                .rateFromRub(new BigDecimal("90.000000"))
                .rateToRub(new BigDecimal("9.000000"))
                .conversionRate(new BigDecimal("10.000000"))
                .resultAmount(new BigDecimal("100.00"))
                .build();
        given(exchangeService.convert(any(ConversionRequest.class))).willReturn(resp);

        String json = """
          { "action": "BUY", "from": "USD", "to": "CNY", "amount": 10.00 }
        """;

        mockMvc.perform(post("/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("CNY"))
                .andExpect(jsonPath("$.resultAmount").value(100.00));

        verify(exchangeService).convert(any(ConversionRequest.class));
    }

    @Test
    void get_operations_sortedByTsDesc() throws Exception {
        var older = ConversionOperation.builder()
                .id(UUID.randomUUID())
                .action("BUY")
                .fromCurrency("USD")
                .toCurrency("RUB")
                .amount(new BigDecimal("1.00"))
                .rateFromRub(new BigDecimal("90.000000"))
                .rateToRub(BigDecimal.ONE)
                .conversionRate(new BigDecimal("90.000000"))
                .resultAmount(new BigDecimal("90.00"))
                .ts(OffsetDateTime.now().minusMinutes(5))
                .build();

        var newer = ConversionOperation.builder()
                .id(UUID.randomUUID())
                .action("SELL")
                .fromCurrency("USD")
                .toCurrency("RUB")
                .amount(new BigDecimal("2.00"))
                .rateFromRub(new BigDecimal("90.000000"))
                .rateToRub(BigDecimal.ONE)
                .conversionRate(new BigDecimal("90.000000"))
                .resultAmount(new BigDecimal("180.00"))
                .ts(OffsetDateTime.now())
                .build();

        given(repo.findAll()).willReturn(List.of(older, newer));

        mockMvc.perform(get("/operations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(newer.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(older.getId().toString()));
    }
}
