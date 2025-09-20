package org.bea;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.function.Supplier;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=front-ui-test",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.enable-auto-commit=false",
        "spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE",
        "topics.exchange.topic=exchange.rates",
        "generator.scheduler.enabled=false",
        "auth.client.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = "exchange.rates")
class FrontUiKafkaIntegrationTest {
    @Autowired
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @AfterEach
    void cleanup() { org.bea.domain.Store.currencyRates.clear(); }

    @MockBean(name = "clientCredentialsTokenSupplier")
    Supplier<String> tokenSupplier;


    @Test
    void shouldConsumeRatesBatchAndUpdateStore() {
        String payload = """
      [
        {"title":"Доллар США","name":"USD","value":95.50},
        {"title":"Евро","name":"EUR","value":101.20}
      ]
      """;

        kafkaTemplate.send("exchange.rates", "batch-1", payload);

        org.awaitility.Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var map = org.bea.domain.Store.currencyRates;
                    org.assertj.core.api.Assertions.assertThat(map).containsKeys("USD", "EUR");
                });
    }
}
