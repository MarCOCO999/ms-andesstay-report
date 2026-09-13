package com.andesstay.ms_andesstay_report.domain;

/**
 * Los mismos literales de estado que ms-andesstay-reservations serializa en el JSON del
 * evento (ver ReservationStatus alla: EN_ESTADIA se serializa como "EN_ESTADÍA" con tilde
 * para respetar el contrato de la API definido en el caso).
 */
public final class ReservationStatusLiterals {

    public static final String CREADA = "CREADA";
    public static final String EN_ESTADIA = "EN_ESTADÍA";
    public static final String CHECKOUT = "CHECKOUT";

    private ReservationStatusLiterals() {
    }
}
