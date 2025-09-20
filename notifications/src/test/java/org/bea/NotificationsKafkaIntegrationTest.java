package org.bea;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.bea.dto.NotificationDto;
import org.bea.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=notifications",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "topics.notifications.topic=test",
        "spring.kafka.consumer.enable-auto-commit=false",
        "spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE",
})
@EmbeddedKafka(partitions = 1, topics = { "test" })
class NotificationsKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldConsumeEventFromCashTopic() {
        // given
        NotificationService.messages.clear();
        var evt = new NotificationDto("op-1", "qq", "100.00", "RUB");
        // when
        kafkaTemplate.send("test", "op-1", evt.toJson());

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(NotificationService.messages).isNotEmpty();
                    var text = NotificationService.messages.peek();
                    assertThat(text).contains("100.00").contains("RUB");
                });
    }
}
