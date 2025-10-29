package com.test.mysede.auth;

/**
 * Enum que define los permisos específicos del sistema
 * Basado en los requisitos funcionales del documento
 */
public enum Permiso {

    // ========== GESTIÓN DE USUARIOS (Solo Administrador) ==========
    CREAR_USUARIO("Crear nuevos usuarios", Categoria.USUARIOS),
    EDITAR_USUARIO("Editar usuarios existentes", Categoria.USUARIOS),
    ELIMINAR_USUARIO("Eliminar usuarios", Categoria.USUARIOS),
    VER_USUARIOS("Ver lista de usuarios", Categoria.USUARIOS),
    ASIGNAR_PERMISOS("Asignar permisos y roles", Categoria.USUARIOS),

    // ========== GESTIÓN DE ACTIVIDADES ==========
    CREAR_ACTIVIDAD("Crear nuevas actividades", Categoria.ACTIVIDADES),
    EDITAR_ACTIVIDAD("Modificar actividades", Categoria.ACTIVIDADES),
    ELIMINAR_ACTIVIDAD("Cancelar/Eliminar actividades", Categoria.ACTIVIDADES),
    VER_ACTIVIDADES("Visualizar actividades", Categoria.ACTIVIDADES),

    // ========== GESTIÓN DE CITAS ==========
    CREAR_CITA("Crear nuevas citas", Categoria.CITAS),
    EDITAR_CITA("Reagendar citas", Categoria.CITAS),
    ELIMINAR_CITA("Cancelar citas", Categoria.CITAS),
    VER_CITAS("Visualizar citas", Categoria.CITAS),

    // ========== GESTIÓN DE ARCHIVOS ==========
    ADJUNTAR_ARCHIVOS("Adjuntar archivos a actividades", Categoria.ARCHIVOS),
    VER_ARCHIVOS("Ver archivos adjuntos", Categoria.ARCHIVOS),
    ELIMINAR_ARCHIVOS("Eliminar archivos adjuntos", Categoria.ARCHIVOS),

    // ========== CALENDARIO ==========
    VER_CALENDARIO("Visualizar calendario", Categoria.CALENDARIO),

    // ========== MANTENEDORES (Solo Administrador) ==========
    GESTIONAR_TIPOS_ACTIVIDAD("Gestionar tipos de actividad", Categoria.MANTENEDORES),
    GESTIONAR_LUGARES("Gestionar lugares/salas", Categoria.MANTENEDORES),
    GESTIONAR_OFERENTES("Gestionar oferentes", Categoria.MANTENEDORES),
    GESTIONAR_SOCIOS("Gestionar socios comunitarios", Categoria.MANTENEDORES),
    GESTIONAR_PROYECTOS("Gestionar proyectos", Categoria.MANTENEDORES),

    // ========== NOTIFICACIONES ==========
    RECIBIR_NOTIFICACIONES("Recibir notificaciones de recordatorio", Categoria.NOTIFICACIONES),
    SUPERVISAR_NOTIFICACIONES("Supervisar sistema de notificaciones", Categoria.NOTIFICACIONES);

    private final String descripcion;
    private final Categoria categoria;

    Permiso(String descripcion, Categoria categoria) {
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Categorías de permisos para organización
     */
    public enum Categoria {
        USUARIOS("Gestión de Usuarios"),
        ACTIVIDADES("Gestión de Actividades"),
        CITAS("Gestión de Citas"),
        ARCHIVOS("Gestión de Archivos"),
        CALENDARIO("Calendario"),
        MANTENEDORES("Mantenedores"),
        NOTIFICACIONES("Notificaciones");

        private final String nombre;

        Categoria(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }
}