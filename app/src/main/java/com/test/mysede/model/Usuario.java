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

    // Constructor vacío
    public Usuario() {
        this.permisos = new HashSet<>();
        this.activo = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.ultimoAcceso = System.currentTimeMillis();
    }

    // Constructor con datos básicos
    public Usuario(String nombre, String email, Rol rol) {
        this();
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.id = generarId(email);
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