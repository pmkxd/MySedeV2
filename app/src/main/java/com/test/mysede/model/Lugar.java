package com.test.mysede.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Representa un lugar donde se ejecutan actividades.
 */
public class Lugar {

    public enum Tipo {
        OFICINA_DEL_CENTRO,
        LUGAR_DEL_TERRITORIO
    }

    private final String nombre;
    private final Tipo tipo;
    private final Integer cupo;

    public Lugar(String nombre, Tipo tipo, Integer cupo) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre del lugar es obligatorio");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de lugar es obligatorio");
        this.cupo = cupo;
    }

    public String getNombre() {
        return nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Optional<Integer> getCupo() {
        return Optional.ofNullable(cupo);
    }
}
