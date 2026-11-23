package com.andesstay.ms_andesstay_report.web.dto;

import java.util.Map;

/**
 * Estado actual de las reservas, calculado desde audit.timeline: cuantas hay en cada estado y
 * que rol hizo el ultimo cambio de cada una.
 */
public record StatusSummaryResponse(long total, Map<String, Long> byStatus, Map<String, Long> byLastActorRole) {
}
