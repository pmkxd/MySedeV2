package com.test.mysede.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa la cita de una actividad en un lugar y horario específicos.
 */
public class Cita {

    private final Actividad actividad;
    private final Lugar lugar;
    private final LocalDate fecha;
    private final LocalTime hora;

    public Cita(Actividad actividad, Lugar lugar, LocalDate fecha, LocalTime hora) {
        this.actividad = Objects.requireNonNull(actividad, "La actividad es obligatoria");
        this.lugar = Objects.requireNonNull(lugar, "El lugar es obligatorio");
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.hora = Objects.requireNonNull(hora, "La hora es obligatoria");
    }

    public Actividad getActividad() {
        return actividad;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }
}