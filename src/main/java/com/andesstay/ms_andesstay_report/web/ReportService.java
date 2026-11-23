package com.andesstay.ms_andesstay_report.web;

import com.andesstay.ms_andesstay_report.domain.ReservationStatusLiterals;
import com.andesstay.ms_andesstay_report.repository.ReservationEventFactRepository;
import com.andesstay.ms_andesstay_report.repository.ReservationSnapshotRepository;
import com.andesstay.ms_andesstay_report.web.dto.HourlyCount;
import com.andesstay.ms_andesstay_report.web.dto.KpisResponse;
import com.andesstay.ms_andesstay_report.web.dto.StatusSummaryResponse;
import com.andesstay.ms_andesstay_report.web.dto.TopUnitResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReservationEventFactRepository repository;
    private final ReservationSnapshotRepository snapshotRepository;

    public ReportService(ReservationEventFactRepository repository, ReservationSnapshotRepository snapshotRepository) {
        this.repository = repository;
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional(readOnly = true)
    public KpisResponse kpis(String range) {
        RangeParser.Range parsed = RangeParser.parse(range);

        List<HourlyCount> perHour = repository.reservationsPerHour(parsed.from(), parsed.to(), ReservationStatusLiterals.CREADA)
                .stream()
                .map(row -> new HourlyCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        Double avgCycleTime = repository.averageCycleTimeMinutes(
                parsed.from(), parsed.to(), ReservationStatusLiterals.CREADA, ReservationStatusLiterals.CHECKOUT);

        long activeOccupancy = repository.countActiveOccupancy(ReservationStatusLiterals.EN_ESTADIA);

        return new KpisResponse(range == null ? "last24h" : range, perHour, avgCycleTime, activeOccupancy);
    }

    @Transactional(readOnly = true)
    public List<TopUnitResponse> topUnits(String range, int limit) {
        RangeParser.Range parsed = RangeParser.parse(range);
        return repository.topUnits(parsed.from(), parsed.to(), ReservationStatusLiterals.CREADA, limit)
                .stream()
                .map(row -> new TopUnitResponse(((Number) row[0]).longValue(), ((Number) row[1]).longValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public StatusSummaryResponse statusSummary() {
        Map<String, Long> byStatus = toMap(snapshotRepository.countByStatus());
        Map<String, Long> byRole = toMap(snapshotRepository.countByLastActorRole());
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new StatusSummaryResponse(total, byStatus, byRole);
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        rows.forEach(row -> result.put((String) row[0], ((Number) row[1]).longValue()));
        return result;
    }
}
