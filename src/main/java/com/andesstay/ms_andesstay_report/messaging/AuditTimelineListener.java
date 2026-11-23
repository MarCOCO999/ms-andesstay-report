package com.andesstay.ms_andesstay_report.messaging;

import com.andesstay.ms_andesstay_report.domain.ReservationSnapshot;
import com.andesstay.ms_andesstay_report.service.ReservationSnapshotService;
import jakarta.validation.Valid;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Adaptador de mensajeria de audit.timeline (topico compactado, key = reservationId): mantiene
 * el estado actual de cada reserva via {@link ReservationSnapshotService}.
 * <p>
 * Usa un consumer group propio ("ms-andesstay-report-snapshots"), separado del que consume
 * reservations.events dentro de este mismo servicio: cada flujo confirma sus offsets por
 * separado, y el snapshot se puede reconstruir reseteando solo este grupo al inicio del topico
 * (ms-andesstay-kafka-admin: POST /api/kafka/consumer-groups/{grupo}/offsets/reset).
 * <p>
 * spring.json.value.default.type se sobreescribe solo para este listener: los dos topicos
 * comparten la configuracion del consumidor pero llevan DTOs distintos.
 */
@Component
public class AuditTimelineListener {

    private final ReservationSnapshotService snapshotService;

    public AuditTimelineListener(ReservationSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @KafkaListener(
            id = "report-audit-timeline",
            topics = "${andesstay.kafka.topics.audit-timeline}",
            groupId = "${andesstay.kafka.consumer-groups.audit-timeline}",
            concurrency = "${andesstay.kafka.consumer.concurrency}",
            properties = "spring.json.value.default.type=com.andesstay.ms_andesstay_report.messaging.AuditTimelineEntry")
    public void onTimelineEntry(@Payload(required = false) @Valid AuditTimelineEntry entry,
                                @Header(KafkaHeaders.RECEIVED_KEY) String reservationId) {
        // Valor null = tombstone del topico compactado.
        if (entry == null) {
            snapshotService.remove(reservationId);
            return;
        }
        snapshotService.apply(ReservationSnapshot.builder()
                .reservationId(entry.reservationId())
                .status(entry.newStatus())
                .unitId(entry.unitId())
                .lastEventId(entry.eventId())
                .lastActorRole(entry.actorRole())
                .updatedAt(entry.occurredAt())
                .build());
    }
}
