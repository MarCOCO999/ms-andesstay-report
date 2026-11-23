package com.andesstay.ms_andesstay_report.repository;

import com.andesstay.ms_andesstay_report.domain.ReservationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationSnapshotRepository extends JpaRepository<ReservationSnapshot, String> {

    /** [estado, cantidad] de reservas por estado actual. */
    @Query("SELECT s.status, COUNT(s) FROM ReservationSnapshot s GROUP BY s.status ORDER BY s.status")
    List<Object[]> countByStatus();

    /** [rol, cantidad] segun quien hizo el ultimo cambio de cada reserva. */
    @Query("SELECT COALESCE(s.lastActorRole, 'desconocido'), COUNT(s) FROM ReservationSnapshot s GROUP BY s.lastActorRole")
    List<Object[]> countByLastActorRole();
}
