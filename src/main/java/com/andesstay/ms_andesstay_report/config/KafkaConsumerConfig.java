package com.andesstay.ms_andesstay_report.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * 2 reintentos con 2s de espera y luego DLT ("<topico-origen>.DLT"), igual que en
 * ms-andesstay-audit. El resolver de destino se fija explicitamente porque el sufijo
 * por defecto de DeadLetterPublishingRecoverer en esta version es "-dlt", no ".DLT".
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2L));
    }
}
