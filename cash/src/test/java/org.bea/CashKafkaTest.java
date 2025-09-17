package org.bea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bea.domain.CashAccount;
import org.bea.repository.CashAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.sql.init.mode=never",
        "keycloak.url=http://dummy-keycloak",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = "exchange.rates")
class CashKafkaTest {

    @Autowired MockMvc mockMvc;
    @Autowired EmbeddedKafkaBroker broker;
    @MockBean CashAccountRepository cashAccountRepository;
    @MockBean RestTemplate restTemplate;
    @MockBean(name = "dataSourceInitializer") DataSourceInitializer dataSourceInitializer;
    @MockBean DataSource dataSource;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() { if (consumer != null) consumer.close(); }

    @Test
    void withdraw_sendsKafka_and_returns_balance() throws Exception {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("allowed", true));

        // Счёт есть с балансом 150.00
        CashAccount acc = CashAccount.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .currency("USD")
                .balance(new BigDecimal("150.00"))
                .createdAt(Instant.now())
                .build();
        when(cashAccountRepository.findByUsernameAndCurrency("testUser", "USD"))
                .thenReturn(Optional.of(acc));
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var props = KafkaTestUtils.consumerProps("testGroup2", "true", broker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer())
                .createConsumer();
        broker.consumeFromAnEmbeddedTopic(consumer, "exchange.rates");

        mockMvc.perform(post("/user/testUser/getCash")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("action", "PUT")
                        .param("currency", "USD")
                        .param("value", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("testUser"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(250.00))
                .andExpect(jsonPath("$.message").value("Deposited"));

        ConsumerRecord<String, String> rec =
                KafkaTestUtils.getSingleRecord(consumer, "exchange.rates");
        JsonNode json = new ObjectMapper().readTree(rec.value());
        assertThat(json.get("operation").asText()).isEqualTo("Пополнение");
        assertThat(json.get("login").asText()).isEqualTo("testUser");
        assertThat(json.get("amount").asText()).isEqualTo("100.00");
        assertThat(json.get("currency").asText()).isEqualTo("USD");
    }
}
