package org.bea.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bea.domain.CurrencyRate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Отправляет одним сообщением JSON-массив курсов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${topics.exchange-rates:cash.exchange.rates}")
    private String topic;

    public void sendRatesBatch(Collection<CurrencyRate> rates) {
        try {
            String payload = objectMapper.writeValueAsString(rates); // один JSON-массив
            kafkaTemplate.send(topic, payload);
            log.debug("Sent {} rates to topic {}", rates.size(), topic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize rates batch", e);
        }
    }
}
