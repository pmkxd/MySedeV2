package com.test.mysede.model;

import java.util.Objects;

/**
 * Representa un proyecto asociado a una actividad.
 */
public class Proyecto {

    private final String nombre;

    public Proyecto(String nombre) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre del proyecto es obligatorio");
    }

    public String getNombre() {
        return nombre;
    }
}