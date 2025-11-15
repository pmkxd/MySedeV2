package com.test.mysede.notificaciones;

public class Notificacion {
    private String titulo;
    private String mensaje;
    private long fechaHora;

    public Notificacion(String titulo, String mensaje, long fechaHora) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora;
    }

    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public long getFechaHora() { return fechaHora; }
}
