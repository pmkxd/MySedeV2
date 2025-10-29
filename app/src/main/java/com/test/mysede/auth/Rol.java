package com.test.mysede.auth;

/**
 * Enum que define los roles del sistema según el documento de planificación
 */
public enum Rol {
    ADMINISTRADOR(
            "Administrador",
            "Usuario encargado de la gestión de usuarios y mantenedores"
    ),
    ORGANIZADOR_ACTIVIDADES(
            "Organizador de Actividades",
            "Usuario encargado de la gestión de actividades"
    ),
    PROGRAMADOR_CITAS(
            "Programador de Citas",
            "Encargado de la gestión de citas"
    ),
    PUBLICISTA(
            "Publicista",
            "Encargado de la difusión y publicidad de las actividades"
    );

    private final String nombreCompleto;
    private final String descripcion;

    Rol(String nombreCompleto, String descripcion) {
        this.nombreCompleto = nombreCompleto;
        this.descripcion = descripcion;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el rol a partir de su nombre completo
     */
    public static Rol fromNombreCompleto(String nombre) {
        for (Rol rol : values()) {
            if (rol.getNombreCompleto().equals(nombre)) {
                return rol;
            }
        }
        return null;
    }
}