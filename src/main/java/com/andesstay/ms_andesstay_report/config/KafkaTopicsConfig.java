package com.andesstay.ms_andesstay_report.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * report solo produce en los DLT de los dos topicos que consume, asi que declara esos dos
 * (reservations.events.DLT tambien lo declara audit con la misma configuracion; Kafka ignora
 * la segunda creacion). Los topicos de origen los declaran reservations y audit.
 */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic reservationsEventsDeadLetterTopic(KafkaTopicsProperties topics) {
        return topics.reservationsEventsDlt().toNewTopic();
    }

    @Bean
    public NewTopic auditTimelineDeadLetterTopic(KafkaTopicsProperties topics) {
        return topics.auditTimelineDlt().toNewTopic();
    }
}
