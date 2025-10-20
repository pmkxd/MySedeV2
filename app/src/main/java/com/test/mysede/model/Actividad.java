package com.test.mysede.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Modelo que representa una actividad disponible en la sede.
 */
public class Actividad {

    private Proyecto proyecto;
    private Periodicidad periodicidad;
    private String nombre;
    private List<TipoActividad> tiposActividad;
    private Integer cupo;
    private List<OferenteActividad> oferentes;
    private SocioComunitario socioComunitario;
    private List<ArchivoAdjunto> archivosAdjuntos;
    private int diasAvisoPrevio;

    public Actividad(String nombre, Periodicidad periodicidad) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la actividad es obligatorio");
        this.periodicidad = Objects.requireNonNull(periodicidad, "La periodicidad es obligatoria");
        this.tiposActividad = new ArrayList<>();
        this.oferentes = new ArrayList<>();
        this.archivosAdjuntos = new ArrayList<>();
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
}
