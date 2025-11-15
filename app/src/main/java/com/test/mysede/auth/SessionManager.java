package com.test.mysede.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.test.mysede.model.Usuario;

/**
 * Gestor de sesión temporal del usuario
 * Guarda y recupera información del usuario en SharedPreferences
 */
public class SessionManager {

    private static final String PREF_NAME = "MySede_Session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROL = "user_rol";
    private static final String KEY_USER_AVATAR_URL = "user_avatar_url";
    private static final String KEY_USER_AVATAR_PUBLIC_ID = "user_avatar_public_id";
    private static final String KEY_USER_AVATAR_DELETE_TOKEN = "user_avatar_delete_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";

    // Timeout de sesión: 24 horas en milisegundos
    private static final long SESSION_TIMEOUT = 24 * 60 * 60 * 1000L;

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
        // Guardar timestamp de creación de sesión para control de expiración
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.putString(KEY_USER_AVATAR_URL, usuario.getProfileImageUrl());
        editor.putString(KEY_USER_AVATAR_PUBLIC_ID, usuario.getProfileImagePublicId());
        editor.putString(KEY_USER_AVATAR_DELETE_TOKEN, usuario.getProfileImageDeleteToken());
        editor.apply();

        // Configurar en PermissionManager
        PermissionManager.setUsuarioActual(usuario);
    }

    /**
     * Verifica si hay una sesión activa y no ha expirado
     * Si la sesión ha expirado, la cierra automáticamente
     */
    public boolean haySesionActiva() {
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }

        // Verificar si la sesión ha expirado
        long sessionTimestamp = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - sessionTimestamp;

        if (sessionDuration > SESSION_TIMEOUT) {
            // Sesión expirada, cerrarla automáticamente
            cerrarSesion();
            return false;
        }

        // Sesión válida y no expirada
        return true;
    }

    /**
     * Verifica si la sesión está próxima a expirar (menos de 1 hora restante)
     * @return true si quedan menos de 60 minutos para que expire
     */
    public boolean sesionProximaAExpirar() {
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }

        long sessionTimestamp = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - sessionTimestamp;
        long tiempoRestante = SESSION_TIMEOUT - sessionDuration;

        // Retorna true si quedan menos de 60 minutos
        return tiempoRestante < (60 * 60 * 1000L) && tiempoRestante > 0;
    }

    /**
     * Renueva el timestamp de la sesión, extendiendo su tiempo de vida
     */
    public void renovarSesion() {
        if (haySesionActiva()) {
            editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
            editor.apply();
        }
    }

    /**
     * Obtiene el usuario de la sesión actual
     * Mantiene los permisos reales del usuario
     */
    public Usuario obtenerUsuarioSesion() {
        if (!haySesionActiva()) return null;

        Usuario usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual != null) return usuarioActual;

        // Si no hay usuario en memoria, reconstruir desde SharedPreferences
        String id = prefs.getString(KEY_USER_ID, null);
        String nombre = prefs.getString(KEY_USER_NAME, null);
        String email = prefs.getString(KEY_USER_EMAIL, null);
        String rolString = prefs.getString(KEY_USER_ROL, null);
        String avatarUrl = prefs.getString(KEY_USER_AVATAR_URL, Usuario.DEFAULT_PROFILE_IMAGE_URL);
        String avatarPublicId = prefs.getString(KEY_USER_AVATAR_PUBLIC_ID, Usuario.DEFAULT_PROFILE_IMAGE_PUBLIC_ID);
        String avatarDeleteToken = prefs.getString(KEY_USER_AVATAR_DELETE_TOKEN, null);
        if (id == null || nombre == null || email == null || rolString == null) return null;

        Rol rol = Rol.valueOf(rolString);
        Usuario usuario = new Usuario(nombre, email, rol);
        usuario.setId(id);
        usuario.setProfileImageUrl(avatarUrl);
        usuario.setProfileImagePublicId(avatarPublicId);
        usuario.setProfileImageDeleteToken(avatarDeleteToken);

        // Asignar usuario reconstruido a PermissionManager
        PermissionManager.setUsuarioActual(usuario);

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

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Invitado");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public Rol getUserRol() {
        String rolString = prefs.getString(KEY_USER_ROL, null);
        return (rolString != null) ? Rol.valueOf(rolString) : null;
    }
}
