package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.controller.RatesController;
import org.bea.domain.CurrencyRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RatesController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(SharedAppProperties.class)
class RatesControllerWebMvcTest {

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
}
