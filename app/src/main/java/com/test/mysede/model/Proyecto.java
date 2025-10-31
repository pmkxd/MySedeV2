package com.test.mysede.model;

import java.util.Objects;

/**
 * Representa un proyecto asociado a una actividad.
 */
public class Proyecto {
    private String id;
    private final String nombre;

    public Proyecto(String nombre) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre del proyecto es obligatorio");
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
}