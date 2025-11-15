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
        editor.putString(KEY_USER_AVATAR_URL, usuario.getProfileImageUrl());
        editor.putString(KEY_USER_AVATAR_PUBLIC_ID, usuario.getProfileImagePublicId());
        editor.putString(KEY_USER_AVATAR_DELETE_TOKEN, usuario.getProfileImageDeleteToken());
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
