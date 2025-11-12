package com.test.mysede.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Plantillas de permisos predefinidas según el rol
 * Basado en la tabla de roles del documento de planificación
 */
public class PlantillaPermisos {

    /**
     * Obtiene los permisos predefinidos para un rol específico
     */
    public static Set<Permiso> obtenerPermisosPorRol(Rol rol) {
        switch (rol) {
            case ADMINISTRADOR:
                return permisosAdministrador();
            case ORGANIZADOR_ACTIVIDADES:
                return permisosOrganizadorActividades();
            case PROGRAMADOR_CITAS:
                return permisosProgramadorCitas();
            case PUBLICISTA:
                return permisosPublicista();
            default:
                return new HashSet<>();
        }
    }

    /**
     * ADMINISTRADOR:
     * - Gestión completa de usuarios
     * - Gestión completa de mantenedores
     * - Supervisión de calendario y notificaciones
     * - Acceso total al sistema
     */
    private static Set<Permiso> permisosAdministrador() {
        return new HashSet<>(Arrays.asList(
                // Usuarios
                Permiso.CREAR_USUARIO,
                Permiso.EDITAR_USUARIO,
                Permiso.ELIMINAR_USUARIO,
                Permiso.VER_USUARIOS,
                Permiso.ASIGNAR_PERMISOS,

                // Actividades
                Permiso.CREAR_ACTIVIDAD,
                Permiso.EDITAR_ACTIVIDAD,
                Permiso.ELIMINAR_ACTIVIDAD,
                Permiso.VER_ACTIVIDADES,

                // Citas
                Permiso.CREAR_CITA,
                Permiso.EDITAR_CITA,
                Permiso.ELIMINAR_CITA,
                Permiso.VER_CITAS,

                // Archivos
                Permiso.ADJUNTAR_ARCHIVOS,
                Permiso.VER_ARCHIVOS,
                Permiso.ELIMINAR_ARCHIVOS,

                // Calendario
                Permiso.VER_CALENDARIO,

                // Mantenedores
                Permiso.GESTIONAR_TIPOS_ACTIVIDAD,
                Permiso.GESTIONAR_LUGARES,
                Permiso.GESTIONAR_OFERENTES,
                Permiso.GESTIONAR_SOCIOS,
                Permiso.GESTIONAR_PROYECTOS,

                // Notificaciones
                Permiso.RECIBIR_NOTIFICACIONES,
                Permiso.SUPERVISAR_NOTIFICACIONES
        ));
    }

    /**
     * ORGANIZADOR DE ACTIVIDADES:
     * - Crear, modificar o cancelar actividades
     * - Crear, modificar o cancelar citas (NUEVO)
     * - Visualizar el calendario de citas
     * - Adjuntar archivos e información relacionada con sus actividades
     * - Recibir notificaciones de recordatorio
     */
    private static Set<Permiso> permisosOrganizadorActividades() {
        return new HashSet<>(Arrays.asList(
                // Actividades
                Permiso.CREAR_ACTIVIDAD,
                Permiso.EDITAR_ACTIVIDAD,
                Permiso.ELIMINAR_ACTIVIDAD,
                Permiso.VER_ACTIVIDADES,

                // Citas (NUEVO - Ahora puede gestionar citas)
                Permiso.CREAR_CITA,
                Permiso.EDITAR_CITA,
                Permiso.ELIMINAR_CITA,
                Permiso.VER_CITAS,

                // Archivos
                Permiso.ADJUNTAR_ARCHIVOS,
                Permiso.VER_ARCHIVOS,

                // Calendario
                Permiso.VER_CALENDARIO,

                // Notificaciones
                Permiso.RECIBIR_NOTIFICACIONES
        ));
    }

    /**
     * PROGRAMADOR DE CITAS:
     * - Crear, reagendar o cancelar citas
     * - Visualizar calendario de citas
     */
    private static Set<Permiso> permisosProgramadorCitas() {
        return new HashSet<>(Arrays.asList(
                // Citas
                Permiso.CREAR_CITA,
                Permiso.EDITAR_CITA,
                Permiso.ELIMINAR_CITA,
                Permiso.VER_CITAS,

                // Calendario
                Permiso.VER_CALENDARIO,

                // Ver actividades (necesario para crear citas)
                Permiso.VER_ACTIVIDADES,

                // Notificaciones
                Permiso.RECIBIR_NOTIFICACIONES
        ));
    }

    /**
     * PUBLICISTA:
     * - Visualizar el calendario de citas
     * - Visualizar actividades
     * - Adjuntar archivos
     */
    private static Set<Permiso> permisosPublicista() {
        return new HashSet<>(Arrays.asList(
                // Solo visualización
                Permiso.VER_ACTIVIDADES,
                Permiso.VER_CITAS,
                Permiso.VER_CALENDARIO,

                // Archivos
                Permiso.ADJUNTAR_ARCHIVOS,
                Permiso.VER_ARCHIVOS
        ));
    }
}