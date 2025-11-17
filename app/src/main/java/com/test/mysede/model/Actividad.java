package com.test.mysede.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Modelo que representa una actividad disponible en la sede.
 */
public class Actividad {
    private String id;
    private Proyecto proyecto;
    private Periodicidad periodicidad;
    private String nombre;
    private List<TipoActividad> tiposActividad;
    private Integer cupo;
    private List<OferenteActividad> oferentes;
    private SocioComunitario socioComunitario;
    private List<ArchivoAdjunto> archivosAdjuntos;
    private int diasAvisoPrevio;
    private List<Cita> citas;

    public Actividad(String nombre, Periodicidad periodicidad) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la actividad es obligatorio");
        this.periodicidad = Objects.requireNonNull(periodicidad, "La periodicidad es obligatoria");
        this.tiposActividad = new ArrayList<>();
        this.oferentes = new ArrayList<>();
        this.archivosAdjuntos = new ArrayList<>();
        this.citas = new ArrayList<>();  // <-- AGREGAR ESTA LÍNEA
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Periodicidad getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(Periodicidad periodicidad) {
        this.periodicidad = Objects.requireNonNull(periodicidad);
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Objects.requireNonNull(nombre);
    }

    public List<TipoActividad> getTiposActividad() {
        return Collections.unmodifiableList(tiposActividad);
    }

    public void setTiposActividad(List<TipoActividad> tiposActividad) {
        this.tiposActividad = new ArrayList<>(Objects.requireNonNull(tiposActividad));
    }

    public Integer getCupo() {
        return cupo;
    }

    public void setCupo(Integer cupo) {
        this.cupo = cupo;
    }

    public List<OferenteActividad> getOferentes() {
        return Collections.unmodifiableList(oferentes);
    }

    public void setOferentes(List<OferenteActividad> oferentes) {
        this.oferentes = new ArrayList<>(Objects.requireNonNull(oferentes));
    }

    public SocioComunitario getSocioComunitario() {
        return socioComunitario;
    }

    public void setSocioComunitario(SocioComunitario socioComunitario) {
        this.socioComunitario = socioComunitario;
    }

    public List<ArchivoAdjunto> getArchivosAdjuntos() {
        return Collections.unmodifiableList(archivosAdjuntos);
    }

    public void setArchivosAdjuntos(List<ArchivoAdjunto> archivosAdjuntos) {
        this.archivosAdjuntos = new ArrayList<>(Objects.requireNonNull(archivosAdjuntos));
    }

    public int getDiasAvisoPrevio() {
        return diasAvisoPrevio;
    }

    public void setDiasAvisoPrevio(int diasAvisoPrevio) {
        if (diasAvisoPrevio < 0) {
            throw new IllegalArgumentException("Los días de aviso previo no pueden ser negativos");
        }
        this.diasAvisoPrevio = diasAvisoPrevio;
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }

    public Cita crearCita(Lugar lugar, LocalDate fecha, LocalTime hora) {
        return crearCita(lugar, fecha, hora, true); // Validar por defecto
    }

    /**
     * Crear cita con opción de validar o no (útil para cargar datos desde Firestore)
     */
    public Cita crearCita(Lugar lugar, LocalDate fecha, LocalTime hora, boolean validar) {
        Objects.requireNonNull(lugar, "El lugar es obligatorio");
        Objects.requireNonNull(fecha, "La fecha es obligatoria");
        Objects.requireNonNull(hora, "La hora es obligatoria");

        if (periodicidad.getTipo() == Periodicidad.Tipo.PUNTUAL && !citas.isEmpty()) {
            if (validar) {
                throw new IllegalStateException("Una actividad puntual solo puede tener una cita");
            }
        }

        if (validar) {
            periodicidad.getFechaInicio().ifPresent(fechaInicio -> {
                if (fecha.isBefore(fechaInicio)) {
                    // Solo log de warning, no crash
                    android.util.Log.w("Actividad", "La fecha de la cita (" + fecha + ") es anterior al inicio de la periodicidad (" + fechaInicio + ")");
                }
            });

            periodicidad.getFechaFin().ifPresent(fechaFin -> {
                if (fecha.isAfter(fechaFin)) {
                    // Solo log de warning, no crash
                    android.util.Log.w("Actividad", "La fecha de la cita (" + fecha + ") es posterior al fin de la periodicidad (" + fechaFin + ")");
                }
            });
        }

        Cita nuevaCita = new Cita(this, lugar, fecha, hora);
        citas.add(nuevaCita);
        return nuevaCita;
    }
    public void limpiarCitas() {
        citas.clear();
    }

    public LocalDateTime calcularMomentoAviso(Cita cita) {
        Objects.requireNonNull(cita, "La cita es obligatoria");
        if (cita.getActividad() != this) {
            throw new IllegalArgumentException("La cita no pertenece a esta actividad");
        }

        LocalDate fechaCita = cita.getFecha();
        LocalTime horaCita = cita.getHora();
        LocalDateTime fechaHoraCita = LocalDateTime.of(fechaCita, horaCita);
        return fechaHoraCita.minusDays(diasAvisoPrevio);
    }
    public void agregarOCambiarCita(Cita citaActualizada) {
        Objects.requireNonNull(citaActualizada, "La cita es obligatoria");
        String citaId = citaActualizada.getId();
        if (citaId != null) {
            citas.removeIf(cita -> citaId.equals(cita.getId()));
        }
        citas.add(citaActualizada);
    }

    public void eliminarCitaPorId(String citaId) {
        if (citaId == null) return;
        citas.removeIf(cita -> citaId.equals(cita.getId()));
    }
}
