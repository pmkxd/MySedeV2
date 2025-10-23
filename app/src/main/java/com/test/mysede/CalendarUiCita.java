package com.test.mysede;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

final class CalendarUiCita {
    private final UUID id = UUID.randomUUID();
    private final Actividad actividad;
    private final Lugar lugar;
    private LocalDate fecha;
    private LocalTime hora;
    private final int duracionMinutos;

    CalendarUiCita(Cita cita, int duracionMinutos) {
        this(Objects.requireNonNull(cita).getActividad(), cita.getLugar(), cita.getFecha(), cita.getHora(), duracionMinutos);
    }

    CalendarUiCita(Actividad actividad, Lugar lugar, LocalDate fecha, LocalTime hora, int duracionMinutos) {
        this.actividad = Objects.requireNonNull(actividad, "La actividad es obligatoria");
        this.lugar = Objects.requireNonNull(lugar, "El lugar es obligatorio");
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.hora = Objects.requireNonNull(hora, "La hora es obligatoria");
        this.duracionMinutos = duracionMinutos;
    }

    UUID getId() {
        return id;
    }

    Actividad getActividad() {
        return actividad;
    }

    Lugar getLugar() {
        return lugar;
    }

    LocalDate getFecha() {
        return fecha;
    }

    LocalTime getHora() {
        return hora;
    }

    int getDuracionMinutos() {
        return duracionMinutos;
    }

    void actualizarFechaHora(LocalDate nuevaFecha, LocalTime nuevaHora) {
        this.fecha = Objects.requireNonNull(nuevaFecha);
        this.hora = Objects.requireNonNull(nuevaHora);
    }
}