package com.test.mysede.model;

import java.util.Objects;

/**
 * Representa un tipo de actividad disponible en la sede.
 */
public class TipoActividad {

    public enum Categoria {
        CAPACITACION,
        TALLER,
        CHARLA,
        ATENCION,
        OPERATIVO_EN_OFICINA,
        OPERATIVO_RURAL,
        OPERATIVO,
        PRACTICA_PROFESIONAL,
        DIAGNOSTICO
    }

    private final String nombre;
    private final String descripcion;
    private final Categoria categoria;

    public TipoActividad(String nombre, String descripcion, Categoria categoria) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre es obligatorio");
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción es obligatoria");
        this.categoria = Objects.requireNonNull(categoria, "La categoría es obligatoria");
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
