package com.andesstay.ms_andesstay_report.messaging;

import com.andesstay.ms_andesstay_report.domain.ReservationEventFact;
import com.andesstay.ms_andesstay_report.service.ReservationMetricsService;
import jakarta.validation.Valid;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Adaptador de mensajeria de reservations.events: valida el evento, lo traduce a un hecho de
 * dominio y delega en {@link ReservationMetricsService}.
 * <p>
 * Consumer group "ms-andesstay-report", distinto del de audit: ambos servicios reciben todos los
 * eventos y cada uno avanza a su propio ritmo (si report se cae, audit no se atrasa). Dentro del
 * grupo, 3 consumidores (1 por particion) reparten la carga.
 */
@Component
public class ReservationEventListener {

    private final ReservationMetricsService metricsService;

    public ReservationEventListener(ReservationMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @KafkaListener(
            id = "report-reservations-events",
            topics = "${andesstay.kafka.topics.reservations-events}",
            groupId = "${andesstay.kafka.consumer-groups.reservations-events}",
            concurrency = "${andesstay.kafka.consumer.concurrency}")
    public void onReservationEvent(@Payload @Valid ReservationEvent event) {
        metricsService.register(ReservationEventFact.builder()
                .eventId(event.eventId())
                .reservationId(event.reservationId())
                .unitId(event.unitId())
                .previousStatus(event.previousStatus())
                .newStatus(event.newStatus())
                .occurredAt(event.timestamp())
                .build());
    }
}
