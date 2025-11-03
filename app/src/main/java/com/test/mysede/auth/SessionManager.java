package com.test.mysede.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.test.mysede.model.Usuario;

import java.util.HashSet;
import java.util.Set;

/**
 * Gestor de sesión temporal del usuario
 * Guarda y recupera información del usuario en SharedPreferences
 * Este es un sistema temporal hasta que se implemente Firebase Auth
 */
public class SessionManager {

    private static final String PREF_NAME = "MySede_Session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROL = "user_rol";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Guarda la sesión del usuario
     */
    public void crearSesion(Usuario usuario) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, usuario.getId());
        editor.putString(KEY_USER_NAME, usuario.getNombre());
        editor.putString(KEY_USER_EMAIL, usuario.getEmail());
        editor.putString(KEY_USER_ROL, usuario.getRol().name());
        editor.apply();

        // Configurar en PermissionManager
        PermissionManager.setUsuarioActual(usuario);
    }

    /**
     * Verifica si hay una sesión activa
     */
    public boolean haySesionActiva() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtiene el usuario de la sesión actual
     * Nota: Los permisos se asignan según la plantilla del rol
     */
    public Usuario obtenerUsuarioSesion() {
        if (!haySesionActiva()) {
            return null;
        }

        String id = prefs.getString(KEY_USER_ID, null);
        String nombre = prefs.getString(KEY_USER_NAME, null);
        String email = prefs.getString(KEY_USER_EMAIL, null);
        String rolString = prefs.getString(KEY_USER_ROL, null);

        if (id == null || nombre == null || email == null || rolString == null) {
            return null;
        }

        Rol rol = Rol.valueOf(rolString);
        Usuario usuario = new Usuario(nombre, email, rol);
        usuario.setId(id);

        // Asignar permisos según plantilla
        Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);
        usuario.setPermisos(permisos);

        return usuario;
    }

    /**
     * Cierra la sesión del usuario
     */
    public void cerrarSesion() {
        editor.clear();
        editor.apply();
        PermissionManager.cerrarSesion();
    }

    /**
     * Obtiene el ID del usuario logueado
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Obtiene el nombre del usuario logueado
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Invitado");
    }

    /**
     * Obtiene el email del usuario logueado
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Obtiene el rol del usuario logueado
     */
    public Rol getUserRol() {
        String rolString = prefs.getString(KEY_USER_ROL, null);
        if (rolString != null) {
            return Rol.valueOf(rolString);
        }
        return null;
    }
}