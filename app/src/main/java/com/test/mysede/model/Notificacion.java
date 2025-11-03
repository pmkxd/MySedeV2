package com.test.mysede.model;

import com.google.firebase.Timestamp;

public class Notificacion {
    private String id;
    private String titulo;
    private String mensaje;
    private String tipo;        // info / alerta / actividad / sistema
    private boolean leida;
    private Timestamp fecha;    // fecha de creación
    private String idActividad; // opcional, si viene de una actividad

    public Notificacion() {}

    public Notificacion(String titulo, String mensaje, String tipo, Timestamp fecha, boolean leida) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fecha = fecha;
        this.leida = leida;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public String getTipo() { return tipo; }
    public boolean isLeida() { return leida; }
    public Timestamp getFecha() { return fecha; }
    public String getIdActividad() { return idActividad; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public void setIdActividad(String idActividad) { this.idActividad = idActividad; }
}
