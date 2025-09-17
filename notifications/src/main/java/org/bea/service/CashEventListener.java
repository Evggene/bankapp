package org.bea.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bea.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashEventListener {

    @Value("${topics.notifications.cash-topic}")
    private String topic;

    @KafkaListener(
            topics = "${topics.notifications.cash-topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCashEvent(NotificationDto event) {
        log.info("Notification received from topic '{}': {}", topic, event);
        String text = buildText(event);
        NotificationService.messages.offer(text);
    }

    private String buildText(NotificationDto e) {
        String base = "%s %s %s %s".formatted(
                e.getOperation(),
                e.getAmount(),
                e.getCurrency(),
                e.getLogin()
        );
        return base.trim();
    }
}
