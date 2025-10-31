package com.test.mysede.model;

import java.util.Objects;

/**
 * Representa un oferente que imparte la actividad.
 */
public class OferenteActividad {

    public enum Institucion {
        IP,
        CFT,
        UNIVERSIDAD
    }
    private String id;
    private final String nombre;
    private final String docenteResponsable;
    private final Institucion institucion;

    public OferenteActividad(String nombre, String docenteResponsable, Institucion institucion) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre del oferente es obligatorio");
        this.docenteResponsable = Objects.requireNonNull(docenteResponsable, "El docente responsable es obligatorio");
        this.institucion = Objects.requireNonNull(institucion, "La institución es obligatoria");
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

    public String getDocenteResponsable() {
        return docenteResponsable;
    }

    public Institucion getInstitucion() {
        return institucion;
    }
}