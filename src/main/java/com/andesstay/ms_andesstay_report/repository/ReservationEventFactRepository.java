package com.andesstay.ms_andesstay_report.repository;

import com.andesstay.ms_andesstay_report.domain.ReservationEventFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationEventFactRepository extends JpaRepository<ReservationEventFact, Long> {

    boolean existsByEventId(String eventId);

    /** Reservas creadas por hora, dentro del rango solicitado. */
    @Query(value = """
            SELECT TO_CHAR(occurred_at, 'YYYY-MM-DD"T"HH24":00:00Z"') AS hourBucket, COUNT(*) AS total
            FROM reservation_event_facts
            WHERE new_status = :createdStatus
              AND occurred_at BETWEEN :from AND :to
            GROUP BY TO_CHAR(occurred_at, 'YYYY-MM-DD"T"HH24":00:00Z"')
            ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> reservationsPerHour(@Param("from") Instant from, @Param("to") Instant to,
                                        @Param("createdStatus") String createdStatus);

    /** Tiempo de ciclo promedio (minutos) entre CREADA y CHECKOUT, para reservas que cerraron en el rango. */
    @Query(value = """
            SELECT AVG((CAST(checkout_time AS DATE) - CAST(created_time AS DATE)) * 24 * 60)
            FROM (
                SELECT reservation_id,
                       MIN(CASE WHEN new_status = :createdStatus THEN occurred_at END) AS created_time,
                       MAX(CASE WHEN new_status = :checkoutStatus THEN occurred_at END) AS checkout_time
                FROM reservation_event_facts
                WHERE occurred_at BETWEEN :from AND :to
                GROUP BY reservation_id
            )
            WHERE created_time IS NOT NULL AND checkout_time IS NOT NULL
            """, nativeQuery = true)
    Double averageCycleTimeMinutes(@Param("from") Instant from, @Param("to") Instant to,
                                    @Param("createdStatus") String createdStatus, @Param("checkoutStatus") String checkoutStatus);

    /** Reservas cuyo ultimo evento conocido las deja en estadia activa ahora mismo. */
    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT reservation_id, new_status,
                       ROW_NUMBER() OVER (PARTITION BY reservation_id ORDER BY occurred_at DESC) AS rn
                FROM reservation_event_facts
            )
            WHERE rn = 1 AND new_status = :activeStatus
            """, nativeQuery = true)
    long countActiveOccupancy(@Param("activeStatus") String activeStatus);

    /** Unidades mas demandadas (por reservas creadas) dentro del rango, de mas a menos. */
    @Query(value = """
            SELECT unit_id AS unitId, COUNT(*) AS total
            FROM reservation_event_facts
            WHERE new_status = :createdStatus
              AND occurred_at BETWEEN :from AND :to
              AND unit_id IS NOT NULL
            GROUP BY unit_id
            ORDER BY COUNT(*) DESC
            FETCH FIRST :limit ROWS ONLY
            """, nativeQuery = true)
    List<Object[]> topUnits(@Param("from") Instant from, @Param("to") Instant to,
                             @Param("createdStatus") String createdStatus, @Param("limit") int limit);
}
