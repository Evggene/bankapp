package org.bea;

import org.bea.controller.OperationController;
import org.bea.domain.CurrencyRate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OperationController.class)
@AutoConfigureMockMvc(addFilters = false)
class OperationControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    Supplier<String> clientCredentialsToken;

    @MockBean
    RestTemplate restTemplate;

    @Test
    void getRates_proxiesThroughGateway_withBearer() throws Exception {
        when(clientCredentialsToken.get()).thenReturn("tok123");
        List<CurrencyRate> body = List.of(
                new CurrencyRate("RUB","Российский рубль", BigDecimal.ONE),
                new CurrencyRate("USD","Доллар США", new BigDecimal("95.55"))
        );
        ResponseEntity<List<CurrencyRate>> response = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(eq("http://gateway/exchange-generator/getRates"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(response);

        String json = mockMvc.perform(get("/getRates"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(json).contains("[{\"title\":\"RUB\",\"name\":\"Ð Ð¾Ñ\u0081Ñ\u0081Ð¸Ð¹Ñ\u0081ÐºÐ¸Ð¹ Ñ\u0080Ñ\u0083Ð±Ð»Ñ\u008C\",\"value\":1},{\"title\":\"USD\",\"name\":\"Ð\u0094Ð¾Ð»Ð»Ð°Ñ\u0080 Ð¡Ð¨Ð\u0090\",\"value\":95.55}]");
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("http://gateway/exchange-generator/getRates"),
                eq(HttpMethod.GET), captor.capture(), any(ParameterizedTypeReference.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer tok123");
    }

    @Test
    void post_doTransfer_callsTransferServiceThroughGateway() throws Exception {
        when(clientCredentialsToken.get()).thenReturn("tok123");

        mockMvc.perform(post("/user/john/doTransfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("from_currency","USD")
                        .param("to_currency","CNY")
                        .param("value","5.00")
                        .param("to_login","mary"))
                .andExpect(status().isOk());

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("http://gateway/transfer/user/john/doTransfer"), captor.capture(), eq(Void.class));
        HttpEntity<?> entity = captor.getValue();
        assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer tok123");
        assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
        String form = entity.getBody().toString();
        assertThat(form).contains("{from_currency=[USD], to_currency=[CNY], value=[5.00], to_login=[mary]}");
    }
}
