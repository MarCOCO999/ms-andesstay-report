package com.andesstay.ms_andesstay_report.messaging;

import java.time.Instant;

/**
 * Mismo contrato que consume ms-andesstay-audit desde reservations.events, publicado por
 * ms-andesstay-reservations en cada cambio de estado de una reserva.
 */
public record ReservationEvent(
        String type,
        String eventId,
        Instant timestamp,
        String traceId,
        String correlationId,
        String reservationId,
        String previousStatus,
        String newStatus,
        String actorId,
        String actorRole,
        Long unitId
) {
}
