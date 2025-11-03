package com.test.mysede.notificaciones;

public class Notificacion {

    private String id;
    private String titulo;
    private String mensaje;
    private String fecha;
    private boolean leida;

    // Constructor vacío para Firebase
    public Notificacion() {}

    public Notificacion(String id, String titulo, String mensaje, String fecha, boolean leida) {
        this.id = id;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = leida;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
