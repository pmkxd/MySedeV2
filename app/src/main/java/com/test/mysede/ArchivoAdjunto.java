package com.test.mysede;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ArchivoAdjunto implements Parcelable {
    private int id;

    private String nombre;
    private String tipo;
    private long tamaño;
    private Uri uri;

    public ArchivoAdjunto(String nombre, String tipo, long tamaño, Uri uri) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamaño = tamaño;
        this.uri = uri;
    }

    protected ArchivoAdjunto(Parcel in) {
        nombre = in.readString();
        tipo = in.readString();
        tamaño = in.readLong();
        uri = in.readParcelable(Uri.class.getClassLoader());
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

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public long getTamaño() { return tamaño; }
    public Uri getUri() { return uri; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setTamaño(long tamaño) {
        this.tamaño = tamaño;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nombre);
        dest.writeString(tipo);
        dest.writeLong(tamaño);
        dest.writeParcelable(uri, flags);
    }
}
