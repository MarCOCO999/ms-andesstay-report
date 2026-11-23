package com.andesstay.ms_andesstay_report.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Contrato consumido desde audit.timeline, publicado por ms-andesstay-audit despues de auditar
 * cada cambio de estado: el ultimo "quien / que / cuando" de una reserva (key = reservationId).
 */
public record AuditTimelineEntry(
        @NotBlank String eventId,
        @NotBlank String reservationId,
        @NotBlank String eventType,
        String previousStatus,
        @NotBlank String newStatus,
        String actorId,
        String actorRole,
        Long unitId,
        @NotNull Instant occurredAt
) {
}
