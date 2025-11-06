package com.test.mysede.calendar;

import androidx.annotation.Nullable;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

final class CalendarUiCita {
    private final UUID uiId = UUID.randomUUID();

    @Nullable
    private String firestoreId;
    @Nullable
    private String actividadId;
    private String actividadNombre;
    private String lugarNombre;
    private LocalDate fecha;
    private LocalTime hora;
    private final int duracionMinutos;

    @Nullable
    private Cita cita;
    @Nullable
    private Actividad actividad;
    @Nullable
    private Lugar lugar;

    CalendarUiCita(
            @Nullable String firestoreId,
            @Nullable String actividadId,
            String actividadNombre,
            String lugarNombre,
            LocalDate fecha,
            LocalTime hora,
            int duracionMinutos
    ) {
        this.firestoreId = firestoreId;
        this.actividadId = actividadId;
        this.actividadNombre = Objects.requireNonNull(actividadNombre, "El nombre de la actividad es obligatorio");
        this.lugarNombre = Objects.requireNonNull(lugarNombre, "El nombre del lugar es obligatorio");
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.hora = Objects.requireNonNull(hora, "La hora es obligatoria");
        this.duracionMinutos = duracionMinutos;
    }

    static CalendarUiCita fromCita(Cita cita) {
        Objects.requireNonNull(cita, "La cita es obligatoria");
        Actividad actividad = cita.getActividad();
        Lugar lugar = cita.getLugar();

        String nombreActividad = "";
        if (actividad != null) {
            String nombre = actividad.getNombre();
            if (nombre != null) {
                nombreActividad = nombre;
            } else if (actividad.getId() != null) {
                nombreActividad = actividad.getId();
            }
        }

        String nombreLugar = "";
        if (lugar != null && lugar.getNombre() != null) {
            nombreLugar = lugar.getNombre();
        }

        CalendarUiCita uiCita = new CalendarUiCita(
                cita.getId(),
                actividad != null ? actividad.getId() : null,
                nombreActividad,
                nombreLugar,
                cita.getFecha(),
                cita.getHora(),
                60
        );
        uiCita.cita = cita;
        uiCita.actividad = actividad;
        uiCita.lugar = lugar;
        return uiCita;
    }

    UUID getUiId() {
        return uiId;
    }

    @Nullable
    String getFirestoreId() {
        return firestoreId;
    }

    @Nullable
    String getActividadId() {
        return actividadId;
    }

    String getActividadNombre() {
        return actividadNombre;
    }

    String getLugarNombre() {
        return lugarNombre;
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

    @Nullable
    Cita getCita() {
        return cita;
    }

    @Nullable
    Actividad getActividad() {
        return actividad != null ? actividad : (cita != null ? cita.getActividad() : null);
    }

    @Nullable
    Lugar getLugar() {
        return lugar != null ? lugar : (cita != null ? cita.getLugar() : null);
    }

    void actualizarModelo(@Nullable Cita nuevaCita) {
        this.cita = nuevaCita;
        if (nuevaCita != null) {
            this.firestoreId = nuevaCita.getId();

            Actividad nuevaActividad = nuevaCita.getActividad();
            if (nuevaActividad != null) {
                this.actividad = nuevaActividad;
                this.actividadId = nuevaActividad.getId();
                String nombre = nuevaActividad.getNombre();
                if (nombre != null) {
                    this.actividadNombre = nombre;
                } else if (this.actividadId != null) {
                    this.actividadNombre = this.actividadId;
                }
            }

            Lugar nuevoLugar = nuevaCita.getLugar();
            if (nuevoLugar != null) {
                this.lugar = nuevoLugar;
                if (nuevoLugar.getNombre() != null) {
                    this.lugarNombre = nuevoLugar.getNombre();
                }
            }

            this.fecha = nuevaCita.getFecha();
            this.hora = nuevaCita.getHora();
        }
    }

    @Nullable
    Cita crearModelo(LocalDate nuevaFecha, LocalTime nuevaHora) {
        Actividad actividad = getActividad();
        Lugar lugar = getLugar();
        if (actividad == null || lugar == null) {
            return null;
        }
        Cita nuevaCita = new Cita(
                actividad,
                lugar,
                Objects.requireNonNull(nuevaFecha),
                Objects.requireNonNull(nuevaHora)
        );
        if (firestoreId != null && !firestoreId.isEmpty()) {
            nuevaCita.setId(firestoreId);
        }
        return nuevaCita;
    }
}
