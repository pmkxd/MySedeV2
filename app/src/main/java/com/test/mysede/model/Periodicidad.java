package com.test.mysede.model;

import android.os.Build;


import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Describe la periodicidad de una actividad.
 */
public class Periodicidad {

    public enum Tipo {
        PUNTUAL,
        PERIODICA
    }

    private final String nombre;
    private final Tipo tipo;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    private Periodicidad(String nombre, Tipo tipo, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la periodicidad es obligatorio");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de periodicidad es obligatorio");
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;

        if (tipo == Tipo.PUNTUAL && fechaInicio == null) {
            throw new IllegalArgumentException("Una periodicidad puntual requiere una fecha");
        }

        if (tipo == Tipo.PERIODICA) {
            if (fechaInicio == null || fechaFin == null) {
                throw new IllegalArgumentException("Una periodicidad periódica requiere fecha de inicio y fin");
            }
            if (fechaFin.isBefore(fechaInicio)) {
                throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
            }
        }
    }

    public static Periodicidad puntual(String nombre, LocalDate fecha) {
        return new Periodicidad(nombre, Tipo.PUNTUAL, fecha, null);
    }

    public static Periodicidad periodica(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        return new Periodicidad(nombre, Tipo.PERIODICA, fechaInicio, fechaFin);
    }

    public String getNombre() {
        return nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Optional<LocalDate> getFechaInicio() {
        return Optional.ofNullable(fechaInicio);
    }

    public Optional<LocalDate> getFechaFin() {
        return Optional.ofNullable(fechaFin);
    }
}