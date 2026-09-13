package com.andesstay.ms_andesstay_report.web;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Traduce "range=last24h" / "range=last7d" (ver caso, endpoints de report) a un intervalo [from, to]. */
public final class RangeParser {

    private static final Pattern PATTERN = Pattern.compile("^last(\\d+)([hd])$");
    private static final Duration DEFAULT_RANGE = Duration.ofHours(24);

    private RangeParser() {
    }

    public record Range(Instant from, Instant to) {
    }

    public static Range parse(String range) {
        Instant now = Instant.now();
        Duration duration = DEFAULT_RANGE;
        if (range != null) {
            Matcher matcher = PATTERN.matcher(range.trim().toLowerCase());
            if (matcher.matches()) {
                long amount = Long.parseLong(matcher.group(1));
                duration = "h".equals(matcher.group(2)) ? Duration.ofHours(amount) : Duration.ofDays(amount);
            } else {
                throw new InvalidRangeException(range);
            }
        }
        return new Range(now.minus(duration), now);
    }
}
