package org.bea;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bea.dto.NotificationDto;
import org.bea.repo.TransferOperationRepository;
import org.bea.service.TransferService;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест Kafka для модуля transfer:
 * - дергаем контроллер (POST /user/{login}/transfer)
 * - перехватываем запись из топика exchange.rates
 * Примечание: путь эндпоинта предполагаемый. Если у вас другой маппинг —
 * просто поправьте URI ниже.
 */
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.sql.init.mode=never",
        "app.gateway-base-url=http://localhost:9999",
        "keycloak.url=http://dummy-keycloak",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@AutoConfigureMockMvc
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = "notifications.events")
class TransferKafkaMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired EmbeddedKafkaBroker broker;
    @MockBean RestTemplate restTemplate;
    @MockBean TransferOperationRepository transferOperationRepository;
    @MockBean(name = "dataSourceInitializer") DataSourceInitializer dataSourceInitializer;
    @MockBean DataSource dataSource;
    @MockBean TransferService transferService;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close();
    }

    @Test
    void transfer_emitsKafkaNotification_andReturns204() throws Exception {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Map.of("allowed", true));
        when(transferOperationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var props = KafkaTestUtils.consumerProps("transferTestGroup", "true", broker);
        consumer = new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer()
        ).createConsumer();
        broker.consumeFromAnEmbeddedTopic(consumer, "notifications.events");

        var login = "alice";
        mockMvc.perform(post("/user/{login}/doTransfer", login)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from_currency", "USD")
                        .param("to_currency", "EUR")
                        .param("value", "100.00")
                        .param("to_login", "bob"))
                .andExpect(status().isNoContent());

        var rec = KafkaTestUtils.getSingleRecord(consumer, "notifications.events");
        assertThat(rec.value()).contains("Перевод");
        assertThat(rec.value()).contains("alice", "bob");
        assertThat(rec.value()).contains("USD", "EUR");
        assertThat(rec.value()).contains("100.00");
    }
}
