package org.bea.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.bea.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(NotificationDto notification) {
        kafkaTemplate.send("notifications.event", notification.toJson());
    }
}
