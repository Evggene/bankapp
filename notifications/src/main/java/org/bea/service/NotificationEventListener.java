package org.bea.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bea.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    @Value("${topics.notifications.cash-topic}")
    private String topic;

    @KafkaListener(
            topics = "${topics.notifications.cash-topic}"
    )
    public void onCashEvent(ConsumerRecord<String, NotificationDto> record, Acknowledgment ack) {
        NotificationDto event = record.value();
        try {
            log.info("Notification received from topic='{}', partition={}, offset={}: {}",
                    record.topic(), record.partition(), record.offset(), event);

            String text = buildText(event);
            NotificationService.messages.offer(text);

            // Ручной коммит ОФСЕТА — после успешной бизнес-логики
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Ошибка обработки сообщения, offset={}, partition={}. Сообщение будет повторно прочитано.",
                    record.offset(), record.partition(), ex);
        }
    }

    private String buildText(NotificationDto e) {
        String base = "%s %s %s %s".formatted(e.getOperation(), e.getAmount(), e.getCurrency(), e.getLogin());
        return base.trim();
    }
}
