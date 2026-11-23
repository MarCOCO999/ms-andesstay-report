package com.andesstay.ms_andesstay_report.service;

import com.andesstay.ms_andesstay_report.domain.ReservationEventFact;
import com.andesstay.ms_andesstay_report.domain.ReservationSnapshot;
import com.andesstay.ms_andesstay_report.repository.ReservationEventFactRepository;
import com.andesstay.ms_andesstay_report.repository.ReservationSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationSnapshotServiceTest {

    private static final Instant T1 = Instant.parse("2026-11-08T12:00:00Z");
    private static final Instant T2 = Instant.parse("2026-11-08T13:00:00Z");

    private ReservationSnapshotRepository repository;
    private ReservationSnapshotService service;

    @BeforeEach
    void setUp() {
        repository = mock(ReservationSnapshotRepository.class);
        service = new ReservationSnapshotService(repository);
    }

    @Test
    void aplicaElPrimerEstadoDeUnaReserva() {
        when(repository.findById("10")).thenReturn(Optional.empty());

        assertThat(service.apply(snapshot("CREADA", T1))).isTrue();
        verify(repository).save(any());
    }

    @Test
    void unaEntradaMasAntiguaQueLaAplicadaSeDescarta() {
        when(repository.findById("10")).thenReturn(Optional.of(snapshot("CONFIRMADA", T2)));

        assertThat(service.apply(snapshot("CREADA", T1))).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void reaplicarLaMismaEntradaEsIdempotente() {
        when(repository.findById("10")).thenReturn(Optional.of(snapshot("CONFIRMADA", T2)));

        assertThat(service.apply(snapshot("CONFIRMADA", T2))).isTrue();
    }

    @Test
    void lasMetricasIgnoranUnEventoYaRegistrado() {
        ReservationEventFactRepository facts = mock(ReservationEventFactRepository.class);
        when(facts.existsByEventId("e-1")).thenReturn(true);
        ReservationMetricsService metrics = new ReservationMetricsService(facts);

        assertThat(metrics.register(ReservationEventFact.builder().eventId("e-1").build())).isFalse();
        verify(facts, never()).save(any());
    }

    private static ReservationSnapshot snapshot(String status, Instant at) {
        return ReservationSnapshot.builder().reservationId("10").status(status).lastEventId("e-" + status)
                .lastActorRole("Operador").unitId(3L).updatedAt(at).build();
    }
}
