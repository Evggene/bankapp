package org.bea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bea.domain.CurrencyRate;
import org.bea.scheduler.ExchangeScheduler;
import org.bea.service.ExchangeProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.task.scheduling.enabled=false",
        "topics.exchange-rates=exchange.rates"
})
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = "exchange.rates")
class ExchangeKafkaBatchTest {

    @Autowired EmbeddedKafkaBroker broker;
    @Autowired ExchangeProducer producer;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close();
    }

    @Test
    void sendRatesBatch_sendsSingleJsonArrayToTopic() throws Exception {
        var consumerProps = KafkaTestUtils.consumerProps("exchangeTestGroup", "true", broker);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new StringDeserializer()
        ).createConsumer();
        broker.consumeFromAnEmbeddedTopic(consumer, "exchange.rates");

        var batch = List.of(
                new CurrencyRate("Доллар США", "USD", new BigDecimal("95.50")),
                new CurrencyRate("Евро",        "EUR", new BigDecimal("101.20"))
        );
        producer.sendRatesBatch(batch);

        var rec = KafkaTestUtils.getRecords(consumer);
        var first = rec.iterator().next();

        var mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(first.value());

        assertThat(root.isArray()).isTrue();

        JsonNode usd = root.get(0);
        JsonNode eur = root.get(1);

        assertThat(usd.get("name").asText()).isEqualTo("USD");
        assertThat(usd.get("title").asText()).contains("Доллар");
        assertThat(usd.get("value").decimalValue()).isEqualByComparingTo("95.50");

        assertThat(eur.get("name").asText()).isEqualTo("EUR");
        assertThat(eur.get("title").asText()).contains("Евро");
        assertThat(eur.get("value").decimalValue()).isEqualByComparingTo("101.20");
    }
}
