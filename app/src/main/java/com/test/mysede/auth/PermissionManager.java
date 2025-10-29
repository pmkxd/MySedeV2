package com.test.mysede.auth;

import com.test.mysede.model.Usuario;

/**
 * Gestor centralizado de permisos del sistema
 * Controla el acceso a funcionalidades según el rol y permisos del usuario
 */
public class PermissionManager {

    private static Usuario usuarioActual;

    /**
     * Establece el usuario que ha iniciado sesión
     */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
        if (usuario != null) {
            usuario.setUltimoAcceso(System.currentTimeMillis());
        }
    }

    /**
     * Obtiene el usuario actualmente logueado
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay un usuario logueado
     */
    public static boolean hayUsuarioLogueado() {
        return usuarioActual != null;
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public static void cerrarSesion() {
        usuarioActual = null;
    }

    /**
     * Verifica si el usuario actual tiene un permiso específico
     *
     * @param permiso Permiso a verificar
     * @return true si tiene el permiso, false en caso contrario
     */
    public static boolean tienePermiso(Permiso permiso) {
        if (!hayUsuarioLogueado()) {
            return false;
        }

        // Si el usuario está inactivo, no tiene permisos
        if (!usuarioActual.isActivo()) {
            return false;
        }

        // Verificar si el usuario tiene el permiso explícitamente
        return usuarioActual.tienePermiso(permiso);
    }

    /**
     * Verifica si el usuario actual tiene el rol especificado
     *
     * @param rol Rol a verificar
     * @return true si tiene el rol, false en caso contrario
     */
    public static boolean tieneRol(Rol rol) {
        if (!hayUsuarioLogueado()) {
            return false;
        }
        return usuarioActual.getRol() == rol;
    }

    /**
     * Verifica si el usuario actual es Administrador
     */
    public static boolean esAdministrador() {
        return tieneRol(Rol.ADMINISTRADOR);
    }

    /**
     * Verifica si el usuario tiene cualquiera de los permisos especificados
     *
     * @param permisos Array de permisos a verificar
     * @return true si tiene al menos uno de los permisos
     */
    public static boolean tieneCualquierPermiso(Permiso... permisos) {
        for (Permiso permiso : permisos) {
            if (tienePermiso(permiso)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el usuario tiene todos los permisos especificados
     *
     * @param permisos Array de permisos a verificar
     * @return true si tiene todos los permisos
     */
    public static boolean tieneTodosLosPermisos(Permiso... permisos) {
        for (Permiso permiso : permisos) {
            if (!tienePermiso(permiso)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtiene el nombre del usuario actual
     */
    public static String getNombreUsuarioActual() {
        if (hayUsuarioLogueado()) {
            return usuarioActual.getNombre();
        }
        return "Invitado";
    }

    /**
     * Obtiene el rol del usuario actual como String
     */
    public static String getRolUsuarioActual() {
        if (hayUsuarioLogueado()) {
            return usuarioActual.getRol().getNombreCompleto();
        }
        return "Sin rol";
    }
}