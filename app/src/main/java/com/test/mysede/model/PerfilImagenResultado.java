package com.test.mysede.model;

import androidx.annotation.Nullable;

public class PerfilImagenResultado {
    private final String url;
    private final String publicId;
    private final String deleteToken;

    public PerfilImagenResultado(@Nullable String url, @Nullable String publicId, @Nullable String deleteToken) {
        this.url = url;
        this.publicId = publicId;
        this.deleteToken = deleteToken;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getPublicId() {
        return publicId;
    }

    @Nullable
    public String getDeleteToken() {
        return deleteToken;
    }
}