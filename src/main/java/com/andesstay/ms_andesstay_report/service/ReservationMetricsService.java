package com.andesstay.ms_andesstay_report.service;

import com.andesstay.ms_andesstay_report.domain.ReservationEventFact;
import com.andesstay.ms_andesstay_report.repository.ReservationEventFactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra cada cambio de estado como un hecho para los KPIs (reservas por hora, tiempo de
 * ciclo, ocupacion, unidades top). Sin dependencias de Kafka; idempotente por eventId porque
 * Kafka entrega "al menos una vez".
 */
@Service
public class ReservationMetricsService {

    private static final Logger log = LoggerFactory.getLogger(ReservationMetricsService.class);

    private final ReservationEventFactRepository repository;

    public ReservationMetricsService(ReservationEventFactRepository repository) {
        this.repository = repository;
    }

    /** @return true si el hecho es nuevo, false si ya estaba registrado (redelivery). */
    @Transactional
    public boolean register(ReservationEventFact fact) {
        if (repository.existsByEventId(fact.getEventId())) {
            log.info("Evento {} ya estaba en las metricas, se ignora el redelivery", fact.getEventId());
            return false;
        }
        repository.save(fact);
        log.info("Metrica registrada: reserva {} -> {}", fact.getReservationId(), fact.getNewStatus());
        return true;
    }
}
