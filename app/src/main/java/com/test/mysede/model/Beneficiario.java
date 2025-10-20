package com.test.mysede.model;

import java.util.Objects;

/**
 * Representa la caracterización de un beneficiario del servicio.
 */
public class Beneficiario {

    private final String caracterizacion;

    public Beneficiario(String caracterizacion) {
        this.caracterizacion = Objects.requireNonNull(caracterizacion, "La caracterización es obligatoria");
    }

    public String getCaracterizacion() {
        return caracterizacion;
    }
}