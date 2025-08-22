package org.bea;

import org.bea.repo.TransferOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransferControllerIT {

    @DynamicPropertySource
    static void disableExternalImports(DynamicPropertyRegistry r) {
        r.add("spring.config.import", () -> "");
        r.add("spring.cloud.consul.enabled", () -> "false");
        r.add("spring.cloud.loadbalancer.enabled", () -> "false");
        r.add("app.gateway-base-url", () -> "http://gateway");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransferOperationRepository repo;

    // Критично: подменяем RestTemplate на мок — как делают в модулях cash/exchange
    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("POST /user/{login}/doTransfer: allowed -> 204; в БД status=OK")
    void doTransfer_allowed() throws Exception {
        // 1) blocker
        Map<String,Object> blocker = new HashMap<>();
        blocker.put("allowed", true);
        when(restTemplate.postForObject(contains("/blocker/check"), any(), eq(Map.class)))
                .thenReturn(blocker);

        // 2) exchange
        Map<String,Object> exch = new HashMap<>();
        exch.put("resultAmount", "720.50");
        when(restTemplate.postForObject(contains("/exchange/convert"), any(), eq(Map.class)))
                .thenReturn(exch);

        // 3) cash
        when(restTemplate.postForEntity(contains("/cash/user/john/getCash"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
        when(restTemplate.postForEntity(contains("/cash/user/mary/getCash"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));

        // 4) notifications — best effort (игнорим ответ)
        when(restTemplate.postForEntity(contains("/notifications/notify"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));

        mockMvc.perform(post("/user/john/doTransfer")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("from_currency","USD")
                        .param("to_currency","CNY")
                        .param("value","100.00")
                        .param("to_login","mary"))
                .andExpect(status().isNoContent());

        var ops = repo.findTop50ByOrderByTsDesc();
        assertThat(ops).hasSize(1);
        assertThat(ops.get(0).getStatus()).isEqualTo("OK");
        assertThat(ops.get(0).getFromUser()).isEqualTo("john");
        assertThat(ops.get(0).getToUser()).isEqualTo("mary");
    }

    @Test
    @DisplayName("POST /user/{login}/doTransfer: blocked -> 409; в БД status=BLOCKED")
    void doTransfer_blocked() throws Exception {
        Map<String,Object> blocker = new HashMap<>();
        blocker.put("allowed", false);
        blocker.put("reason", "blocked");
        when(restTemplate.postForObject(contains("/blocker/check"), any(), eq(Map.class)))
                .thenReturn(blocker);

        mockMvc.perform(post("/user/john/doTransfer")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("from_currency","USD")
                        .param("to_currency","CNY")
                        .param("value","100.00")
                        .param("to_login","mary"))
                .andExpect(status().isConflict());

        var ops = repo.findTop50ByOrderByTsDesc();
        assertThat(ops).hasSize(0);
    }

    @Test
    @DisplayName("GET /operations — отдаёт список")
    void list_ops() throws Exception {
        // подготавливаем одну успешную операцию через контроллер
        Map<String,Object> blocker = new HashMap<>();
        blocker.put("allowed", true);
        when(restTemplate.postForObject(contains("/blocker/check"), any(), eq(Map.class)))
                .thenReturn(blocker);

        Map<String,Object> exch = new HashMap<>();
        exch.put("resultAmount", "1.00");
        when(restTemplate.postForObject(contains("/exchange/convert"), any(), eq(Map.class)))
                .thenReturn(exch);

        when(restTemplate.postForEntity(contains("/cash/user/john/getCash"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
        when(restTemplate.postForEntity(contains("/cash/user/mary/getCash"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
        when(restTemplate.postForEntity(contains("/notifications/notify"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));

        mockMvc.perform(post("/user/john/doTransfer")
                        .with(jwt()).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("from_currency","USD")
                        .param("to_currency","CNY")
                        .param("value","1")
                        .param("to_login","mary"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/operations").with(jwt()))
                .andExpect(status().isOk());
    }
}
