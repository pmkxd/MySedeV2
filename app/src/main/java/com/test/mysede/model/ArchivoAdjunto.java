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
    private Uri uri; // Se usa mientras el archivo no se sube a la nube
    private String resourceType;
    private String uploadPreset;
    private String publicId;

    public ArchivoAdjunto(String nombre, String tipo, long tamaño, Uri uri, String url) {
        this(nombre, tipo, tamaño, uri, url, null, null, null);
    }

    public ArchivoAdjunto(String nombre, String tipo, long tamaño, Uri uri, String url, String resourceType, String uploadPreset) {
        this(nombre, tipo, tamaño, uri, url, resourceType, uploadPreset, null);
    }

    public ArchivoAdjunto(String nombre, String tipo, long tamaño, Uri uri, String url, String resourceType, String uploadPreset, String publicId) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamaño = tamaño;
        this.url = url;
        this.uri = uri;
        this.resourceType = resourceType;
        this.uploadPreset = uploadPreset;
        this.publicId = publicId;
    }

    protected ArchivoAdjunto(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        tipo = in.readString();
        tamaño = in.readLong();
        uri = in.readParcelable(Uri.class.getClassLoader());
        url = in.readString();
        resourceType = in.readString();
        uploadPreset = in.readString();
        publicId = in.readString();
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
    public String getUrl() { return url; }
    public Uri getUri() { return uri; }
    public String getResourceType() { return resourceType; }
    public String getUploadPreset() { return uploadPreset; }
    public String getPublicId() { return publicId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setTamaño(long tamaño) { this.tamaño = tamaño; }
    public void setUrl(String url) { this.url = url; }
    public void setUri(Uri uri) { this.uri = uri; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public void setUploadPreset(String uploadPreset) { this.uploadPreset = uploadPreset; }
    public void setCloudinaryPublicId(String publicId) { this.publicId = publicId; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nombre);
        dest.writeString(tipo);
        dest.writeLong(tamaño);
        dest.writeParcelable(uri, flags);
        dest.writeString(url);
        dest.writeString(resourceType);
        dest.writeString(uploadPreset);
        dest.writeString(publicId);
    }
}
