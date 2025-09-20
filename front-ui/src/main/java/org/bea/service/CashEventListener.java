package org.bea.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bea.domain.CurrencyRate;
import org.bea.domain.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashEventListener {

    @Value("${topics.exchange.topic:notifications.events}")
    private String topic;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${topics.notifications.cash-topic:exchange.rates}")
    public void onCashEvent(String payload, Acknowledgment ack) {
        try {
            List<CurrencyRate> rates = objectMapper.readValue(payload, new TypeReference<List<CurrencyRate>>() {});
            for (var r : rates) {
                Store.currencyRates.put(r.getName(), r);
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Ошибка обработки батча; сообщение будет перечитано", ex);
            // ack НЕ вызываем — сообщение перечитается согласно политике ретраев
        }
    }
}
