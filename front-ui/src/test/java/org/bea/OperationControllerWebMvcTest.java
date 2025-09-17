package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.controller.OperationController;
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

@WebMvcTest(
        controllers = OperationController.class,
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
                .andExpect(status().is3xxRedirection());

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
