package com.andesstay.ms_andesstay_report.service;

import com.andesstay.ms_andesstay_report.domain.ReservationSnapshot;
import com.andesstay.ms_andesstay_report.repository.ReservationSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantiene el estado actual de cada reserva (modelo de lectura) a partir de audit.timeline.
 * Aplicar la misma entrada dos veces deja el mismo resultado, y una entrada mas antigua que la
 * ya aplicada se descarta: el resultado es correcto aunque haya redelivery o se relea el topico
 * compactado desde el inicio.
 */
@Service
public class ReservationSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(ReservationSnapshotService.class);

    private final ReservationSnapshotRepository repository;

    public ReservationSnapshotService(ReservationSnapshotRepository repository) {
        this.repository = repository;
    }

    /** @return true si el snapshot cambio. */
    @Transactional
    public boolean apply(ReservationSnapshot incoming) {
        ReservationSnapshot current = repository.findById(incoming.getReservationId()).orElse(null);
        if (current != null && current.getUpdatedAt().isAfter(incoming.getUpdatedAt())) {
            log.info("Reserva {}: se descarta la entrada {} ({}), es anterior al estado ya aplicado ({})",
                    incoming.getReservationId(), incoming.getLastEventId(), incoming.getUpdatedAt(), current.getUpdatedAt());
            return false;
        }
        repository.save(incoming);
        log.info("Snapshot de la reserva {}: {}", incoming.getReservationId(), incoming.getStatus());
        return true;
    }

    /** Una tombstone en el topico compactado indica que la reserva ya no debe considerarse. */
    @Transactional
    public void remove(String reservationId) {
        if (repository.existsById(reservationId)) {
            repository.deleteById(reservationId);
            log.info("Snapshot de la reserva {} eliminado (tombstone)", reservationId);
        }
    }
}
