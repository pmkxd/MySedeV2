package com.test.mysede.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ArchivoAdjunto implements Parcelable {

    private String id;
    private String nombre;
    private String tipo;
    private long tamaño;
    private String url;
    private Uri uri; // Aún no se sube a Storage, pero se usa localmente

    public ArchivoAdjunto(String nombre, String tipo, long tamaño, Uri uri, String url) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamaño = tamaño;
        this.url = url;
        this.uri = uri;
    }

    protected ArchivoAdjunto(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        tipo = in.readString();
        tamaño = in.readLong();
        uri = in.readParcelable(Uri.class.getClassLoader());
        url = in.readString();
    }

    public static final Creator<ArchivoAdjunto> CREATOR = new Creator<ArchivoAdjunto>() {
        @Override
        public ArchivoAdjunto createFromParcel(Parcel in) {
            return new ArchivoAdjunto(in);
        }

        @Override
        public ArchivoAdjunto[] newArray(int size) {
            return new ArchivoAdjunto[size];
        }
    };

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public long getTamaño() { return tamaño; }
    public String getUrl() {
        url = "http://firebase.com";
        return url;
    }
    public Uri getUri() { return uri; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setTamaño(long tamaño) { this.tamaño = tamaño; }
    public void setUrl(String url) { this.url = url; }
    public void setUri(Uri uri) { this.uri = uri; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nombre);
        dest.writeString(tipo);
        dest.writeLong(tamaño);
        dest.writeParcelable(uri, flags);
    }
}