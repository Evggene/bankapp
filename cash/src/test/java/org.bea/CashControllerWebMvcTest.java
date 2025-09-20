package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.controller.CashController;
import org.bea.domain.dto.CashBalanceResponse;
import org.bea.domain.dto.CashFormRequest;
import org.bea.service.CashService;
import org.bea.service.NotificationProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.org.bouncycastle.pqc.crypto.saber.SABERParameters;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CashController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SharedAppProperties.class)
class CashControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    CashService cashService;
    @MockBean
    RestTemplate restTemplate;
    @MockBean
    NotificationProducer notificationProducer;

    @Test
    void post_getCash_putAction_callsDeposit_andReturnsJson() throws Exception {
        when(cashService.deposit("john", "USD", new BigDecimal("25.50")))
                .thenReturn(new CashBalanceResponse("john","USD", new BigDecimal("125.50"), "Deposited"));

        mockMvc.perform(post("/user/john/getCash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("action","PUT")
                        .param("currency","USD")
                        .param("value","25.50"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("john"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(125.50))
                .andExpect(jsonPath("$.message").value("Deposited"));

        verify(cashService).deposit(eq("john"), eq("USD"), eq(new BigDecimal("25.50")));
    }

    @Test
    void post_getCash_getAction_callsWithdraw() throws Exception {
        when(cashService.withdraw("john", "USD", new BigDecimal("10.00")))
                .thenReturn(new CashBalanceResponse("john","USD", new BigDecimal("90.00"), "Withdrawn"));

        mockMvc.perform(post("/user/john/getCash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("action","GET")
                        .param("currency","USD")
                        .param("value","10.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(90.00))
                .andExpect(jsonPath("$.message").value("Withdrawn"));
    }

    @Test
    void post_getCash_unknownAction_returns400() throws Exception {
        mockMvc.perform(post("/user/john/getCash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("action","SOMETHING")
                        .param("currency","USD")
                        .param("value","5.00"))
                .andExpect(status().is4xxClientError());
    }
}
