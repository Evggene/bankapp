package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.controller.OperationController;
import org.bea.domain.CurrencyRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OperationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SharedAppProperties.class)
class OperationControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    Supplier<String> clientCredentialsToken;

    @MockBean
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        when(clientCredentialsToken.get()).thenReturn("tok123");
    }

    @Test
    void getRates_proxiesThroughGateway_withBearer() throws Exception {
        // given
        List<CurrencyRate> body = List.of(
                new CurrencyRate("RUB","Российский рубль", BigDecimal.ONE),
                new CurrencyRate("USD","Доллар США", new BigDecimal("95.55"))
        );
        ResponseEntity<List<CurrencyRate>> response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))
        ).thenReturn(response);

        // when
        String json = mockMvc.perform(get("/getRates"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Проверяем содержимое как JSON, без проблем с кодировкой
                .andExpect(jsonPath("$[0].title").value("RUB"))
                .andExpect(jsonPath("$[0].name").value("Российский рубль"))
                .andExpect(jsonPath("$[0].value").value(1))
                .andExpect(jsonPath("$[1].title").value("USD"))
                .andExpect(jsonPath("$[1].name").value("Доллар США"))
                .andExpect(jsonPath("$[1].value").value(95.55))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8); // если нужно читать строкой

        // then — проверяем, что прокинули Bearer
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(any(String.class), eq(HttpMethod.GET), captor.capture(), any(ParameterizedTypeReference.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer tok123");

        // (опционально) убедимся, что пришёл корректный JSON
        assertThat(json).contains("Российский рубль", "Доллар США");
    }

    @Test
    void post_doTransfer_callsTransferServiceThroughGateway() throws Exception {
        // given: стаб для postForEntity, чтобы контроллер не упал на null
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // when
        mockMvc.perform(post("/user/john/doTransfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("from_currency","USD")
                        .param("to_currency","CNY")
                        .param("value","5.00")
                        .param("to_login","mary"))
                .andExpect(status().isOk());

        // then — перехватываем отправленную форму и заголовки
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(any(String.class), captor.capture(), eq(Void.class));
        HttpEntity<?> entity = captor.getValue();

        assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer tok123");
        assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);

        // Проверяем форму как MultiValueMap, а не строку
        @SuppressWarnings("unchecked")
        MultiValueMap<String, String> form = (MultiValueMap<String, String>) entity.getBody();
        assertThat(form.getFirst("from_currency")).isEqualTo("USD");
        assertThat(form.getFirst("to_currency")).isEqualTo("CNY");
        assertThat(form.getFirst("value")).isEqualTo("5.00");
        assertThat(form.getFirst("to_login")).isEqualTo("mary");
    }
}
