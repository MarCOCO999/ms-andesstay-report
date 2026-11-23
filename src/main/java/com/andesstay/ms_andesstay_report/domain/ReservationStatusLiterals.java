package com.andesstay.ms_andesstay_report.domain;

/**
 * Los literales de estado tal como llegan en los eventos de Kafka. ms-andesstay-reservations
 * publica Enum.name(), asi que EN_ESTADIA viaja sin tilde; la forma "EN_ESTADÍA" con tilde es
 * solo la del JSON de su API REST (ver ReservationStatus alla).
 */
public final class ReservationStatusLiterals {

    public static final String CREADA = "CREADA";
    public static final String EN_ESTADIA = "EN_ESTADIA";
    public static final String CHECKOUT = "CHECKOUT";

    private ReservationStatusLiterals() {
    }
}
