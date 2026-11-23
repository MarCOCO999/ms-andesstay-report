package com.andesstay.ms_andesstay_report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Topicos con los que trabaja report (andesstay.kafka.topics en application.yml).
 *
 * @param reservationsEvents    consumido (lo declara ms-andesstay-reservations)
 * @param auditTimeline         consumido (lo declara ms-andesstay-audit)
 * @param reservationsEventsDlt DLT de reservations.events, compartido con audit
 * @param auditTimelineDlt      DLT de audit.timeline, al que solo escribe report
 */
@ConfigurationProperties(prefix = "andesstay.kafka.topics")
public record KafkaTopicsProperties(String reservationsEvents, String auditTimeline,
                                    TopicSpec reservationsEventsDlt, TopicSpec auditTimelineDlt) {
}
