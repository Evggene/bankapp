package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.controller.LoginController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SharedAppProperties.class)
class LoginControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean Supplier<String> clientCredentialsToken;
    @MockBean RestTemplate restTemplate;

    @Test
    void editPassword_postsToAccountsViaGateway_withBearer() throws Exception {
        when(clientCredentialsToken.get()).thenReturn("tok123");

        mockMvc.perform(post("/user/mary/editPassword")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("password","p1")
                        .param("confirmPassword","p1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(any(String.class),
                captor.capture(), eq(Void.class), eq("mary"));
        HttpEntity<?> entity = captor.getValue();
        assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer tok123");
        assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
        assertThat(entity.getBody().toString()).contains("{password=[p1], confirmPassword=[p1]}");
    }

    @Test
    void editUserAccounts_postsToAccountsViaGateway_withForm() throws Exception {
        when(clientCredentialsToken.get()).thenReturn("tok123");

        mockMvc.perform(post("/user/john/editUserAccounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","John Wick")
                        .param("birthdate","2000-02-02"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(any(String.class),
                captor.capture(), eq(Void.class), eq("john"));
        HttpEntity<?> entity = captor.getValue();
        assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer tok123");
        assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
        assertThat(entity.getBody().toString()).contains("{name=[John Wick], birthdate=[2000-02-02]}");
    }
}
