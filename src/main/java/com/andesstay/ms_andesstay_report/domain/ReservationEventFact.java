package com.andesstay.ms_andesstay_report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Hecho crudo por evento de reserva, usado solo para calcular KPIs bajo demanda
 * (reservas por hora, tiempo de ciclo, ocupacion activa, unidades mas demandadas).
 * No es el timeline de auditoria (eso lo persiste ms-andesstay-audit en su propia base).
 */
@Entity
@Table(name = "RESERVATION_EVENT_FACTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEventFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EVENT_ID", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "RESERVATION_ID", nullable = false, length = 60)
    private String reservationId;

    @Column(name = "UNIT_ID")
    private Long unitId;

    @Column(name = "PREVIOUS_STATUS", length = 30)
    private String previousStatus;

    @Column(name = "NEW_STATUS", nullable = false, length = 30)
    private String newStatus;

    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;
}
