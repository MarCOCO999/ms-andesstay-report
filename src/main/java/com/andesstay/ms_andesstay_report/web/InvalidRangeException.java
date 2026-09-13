package com.andesstay.ms_andesstay_report.web;

public class InvalidRangeException extends RuntimeException {

    public InvalidRangeException(String range) {
        super("Rango invalido '" + range + "'. Formato esperado: last<N>h o last<N>d (ej. last24h, last7d)");
    }
}
