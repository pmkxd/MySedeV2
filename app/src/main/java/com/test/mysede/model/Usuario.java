package com.test.mysede.model;

import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.Rol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modelo de Usuario del sistema
 * Implementa Serializable para poder pasar entre Activities
 */
public class Usuario implements Serializable {

    private String id;
    private String nombre;
    private String email;
    private String rut;
    private Rol rol;
    private Set<Permiso> permisos;
    private boolean activo;
    private long fechaCreacion;
    private long ultimoAcceso;
    private String profileImageUrl;
    private String profileImagePublicId;
    private String profileImageDeleteToken;

    public static final String DEFAULT_PROFILE_IMAGE_URL = "android.resource://com.test.mysede/drawable/ic_usuario";
    public static final String DEFAULT_PROFILE_IMAGE_PUBLIC_ID = "mysede/avatars/default_placeholder";

    // Constructor vacío
    public Usuario() {
        this.permisos = new HashSet<>();
        this.activo = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.ultimoAcceso = System.currentTimeMillis();
        this.profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;
        this.profileImagePublicId = DEFAULT_PROFILE_IMAGE_PUBLIC_ID;
    }

    // Constructor con datos básicos
    public Usuario(String nombre, String email, Rol rol) {
        this();
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.id = generarId(email);
        this.profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;
        this.profileImagePublicId = DEFAULT_PROFILE_IMAGE_PUBLIC_ID;
    }

    // Genera un ID único basado en el email
    private String generarId(String email) {
        return email.toLowerCase().replace("@", "_").replace(".", "_");
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        if (this.id == null || this.id.isEmpty()) {
            this.id = generarId(email);
        }
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(long ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = (profileImageUrl == null || profileImageUrl.isEmpty())
                ? DEFAULT_PROFILE_IMAGE_URL
                : profileImageUrl;
    }

    public String getProfileImagePublicId() {
        return profileImagePublicId;
    }

    public void setProfileImagePublicId(String profileImagePublicId) {
        this.profileImagePublicId = (profileImagePublicId == null || profileImagePublicId.isEmpty())
                ? DEFAULT_PROFILE_IMAGE_PUBLIC_ID
                : profileImagePublicId;
    }

    public String getProfileImageDeleteToken() {
        return profileImageDeleteToken;
    }

    public void setProfileImageDeleteToken(String profileImageDeleteToken) {
        this.profileImageDeleteToken = profileImageDeleteToken;
    }

    // Métodos de utilidad para permisos
    public void agregarPermiso(Permiso permiso) {
        this.permisos.add(permiso);
    }

    public void removerPermiso(Permiso permiso) {
        this.permisos.remove(permiso);
    }

    public boolean tienePermiso(Permiso permiso) {
        return this.permisos.contains(permiso);
    }

    public void limpiarPermisos() {
        this.permisos.clear();
    }

    public List<Permiso> getPermisosComoLista() {
        return new ArrayList<>(this.permisos);
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    // Método toString para mostrar información del usuario

    @Override
    public String toString() {
        return nombre + " (" + rol.getNombreCompleto() + ")";
    }
}