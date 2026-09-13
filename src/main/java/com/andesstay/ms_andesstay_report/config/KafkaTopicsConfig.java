package com.andesstay.ms_andesstay_report.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * report tambien declara reservations.events (y su DLT) por si arranca antes que audit;
 * Kafka simplemente ignora la creacion si el topico ya existe con la misma configuracion.
 */
@Configuration
public class KafkaTopicsConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaTopicsConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public NewTopic reservationsEventsTopic() {
        return topic(kafkaProperties.topicReservationsEvents());
    }

    @Bean
    public NewTopic reservationsEventsDeadLetterTopic() {
        return topic(kafkaProperties.topicReservationsEvents() + ".DLT");
    }

    private NewTopic topic(String name) {
        return new NewTopic(name, kafkaProperties.partitions(), (short) kafkaProperties.replicationFactor());
    }
}
