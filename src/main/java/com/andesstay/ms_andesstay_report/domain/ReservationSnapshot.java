package com.andesstay.ms_andesstay_report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Estado actual de cada reserva, construido a partir de audit.timeline (una fila por reserva).
 * Es un modelo de lectura: permite responder "cuantas reservas hay en cada estado ahora" sin
 * consultar la base de reservations.
 */
@Entity
@Table(name = "RESERVATION_SNAPSHOTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSnapshot {

    @Id
    @Column(name = "RESERVATION_ID", length = 60)
    private String reservationId;

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @Column(name = "UNIT_ID")
    private Long unitId;

    @Column(name = "LAST_EVENT_ID", nullable = false, length = 100)
    private String lastEventId;

    @Column(name = "LAST_ACTOR_ROLE", length = 30)
    private String lastActorRole;

    /** Momento de negocio del ultimo cambio aplicado; descarta entradas mas antiguas que lleguen tarde. */
    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;
}
