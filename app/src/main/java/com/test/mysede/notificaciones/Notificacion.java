package com.test.mysede.notificaciones;

import java.io.Serializable;

public class Notificacion implements Serializable {
    private String id;
    private String titulo;
    private String mensaje;
    private long fechaHora;
    private String tipo; // "recordatorio", "confirmacion", "cambio", "cancelacion", "nuevo"
    private boolean leida;
    private String actividadId;
    private String citaId;
    private String usuarioId;

    // Constructor vacío para Firebase
    public Notificacion() {
    }

    // Constructor completo
    public Notificacion(String titulo, String mensaje, long fechaHora) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora;
        this.leida = false;
        this.tipo = "general";
    }

    // Constructor con tipo
    public Notificacion(String titulo, String mensaje, long fechaHora, String tipo) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.leida = false;
    }

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public long getFechaHora() { return fechaHora; }
    public String getTipo() { return tipo; }
    public boolean isLeida() { return leida; }
    public String getActividadId() { return actividadId; }
    public String getCitaId() { return citaId; }
    public String getUsuarioId() { return usuarioId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setFechaHora(long fechaHora) { this.fechaHora = fechaHora; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public void setActividadId(String actividadId) { this.actividadId = actividadId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public String toString() {
        return "Notificacion{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipo='" + tipo + '\'' +
                ", leida=" + leida +
                '}';
    }
}