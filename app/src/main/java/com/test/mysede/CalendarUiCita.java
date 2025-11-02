package com.test.mysede;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;
import androidx.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

final class CalendarUiCita {
    private final UUID uiId = UUID.randomUUID();
    private final String firestoreId;
    private final String actividadId;
    private final String actividadNombre;
    private final String lugarNombre;
    private LocalDate fecha;
    private LocalTime hora;
    private final int duracionMinutos;

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
}