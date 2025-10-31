package com.test.mysede.model;

import java.util.Objects;

/**
 * Representa un archivo adjunto asociado a una actividad.
 */
public class ArchivoAdjunto {
    private String id;
    private final String nombre;
    private final String ubicacion;

    public ArchivoAdjunto(String nombre, String ubicacion) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre del archivo es obligatorio");
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación del archivo es obligatoria");
    }

    public String getNombre() {
        return nombre;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    public String getUbicacion() {
        return ubicacion;
    }
}