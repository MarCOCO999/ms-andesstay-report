package com.andesstay.ms_andesstay_report.web.dto;

import java.util.List;

public record KpisResponse(
        String range,
        List<HourlyCount> reservationsPerHour,
        Double avgCycleTimeMinutes,
        long activeOccupancy
) {
}
