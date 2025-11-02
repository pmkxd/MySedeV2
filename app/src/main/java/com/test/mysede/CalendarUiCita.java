package com.test.mysede;

import com.test.mysede.model.Actividad;
import androidx.annotation.Nullable;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

final class CalendarUiCita {
    private final UUID id = UUID.randomUUID();
    @Nullable
    private final String remoteId;
    @Nullable
    private final String actividadId;
    private String actividadNombre;
    private final Lugar lugar;
    private LocalDate fecha;
    private LocalTime hora;
    private final int duracionMinutos;

    CalendarUiCita(Cita cita, int duracionMinutos) {
        this(
                cita != null ? cita.getId() : null,
                cita != null && cita.getActividad() != null ? cita.getActividad().getId() : null,
                cita != null && cita.getActividad() != null ? cita.getActividad().getNombre() : null,
                Objects.requireNonNull(cita).getLugar(),
                cita.getFecha(),
                cita.getHora(),
                duracionMinutos
        );
    }

    CalendarUiCita(@Nullable String remoteId,
                   @Nullable String actividadId,
                   @Nullable String actividadNombre,
                   Lugar lugar,
                   LocalDate fecha,
                   LocalTime hora,
                   int duracionMinutos) {
        this.remoteId = remoteId;
        this.actividadId = actividadId;
        this.actividadNombre = actividadNombre;
        this.lugar = Objects.requireNonNull(lugar, "El lugar es obligatorio");
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.hora = Objects.requireNonNull(hora, "La hora es obligatoria");
        this.duracionMinutos = duracionMinutos;
    }

    UUID getId() {
        return id;
    }

    @Nullable
    String getRemoteId() {
        return remoteId;
    }

    @Nullable
    String getActividadId() {
        return actividadId;
    }

    @Nullable
    String getActividadNombre() {
        return actividadNombre;
    }

    void setActividadNombre(@Nullable String actividadNombre) {
        this.actividadNombre = actividadNombre;
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