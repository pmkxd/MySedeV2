package com.test.mysede.DAO;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.test.mysede.model.PerfilImagenResultado;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class PerfilImagenDAO {

    private static final String CLOUD_NAME = "dgnbyuqyd";
    private static final String RESOURCE_TYPE = "image";
    private static final String UPLOAD_PRESET = "mysede_avatar";
    private static final String AVATAR_FOLDER = "mysede/avatars";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public Task<PerfilImagenResultado> subirAvatar(@NonNull Context context, @NonNull Uri imagenUri) {
        TaskCompletionSource<PerfilImagenResultado> source = new TaskCompletionSource<>();
        String mimeType = context.getContentResolver().getType(imagenUri);
        RequestBody fileBody = buildStreamRequestBody(context.getContentResolver(), imagenUri, mimeType);
        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/" + RESOURCE_TYPE + "/upload";
        String fileName = "avatar_" + System.currentTimeMillis() + obtenerExtension(imagenUri, mimeType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", AVATAR_FOLDER)
                .addFormDataPart("return_delete_token", "1")
                .addFormDataPart("file", fileName, fileBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        executorService.execute(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error al subir imagen: " + response.code());
                }
                if (response.body() == null) {
                    throw new IOException("Respuesta vacía del servidor de archivos");
                }
                JSONObject json = new JSONObject(response.body().string());
                PerfilImagenResultado resultado = new PerfilImagenResultado(
                        json.optString("secure_url", null),
                        json.optString("public_id", null),
                        json.optString("delete_token", null)
                );
                source.setResult(resultado);
            } catch (IOException | JSONException e) {
                source.setException(e);
            }
        });

        return source.getTask();
    }

    public Task<Void> eliminarAvatarPorToken(@Nullable String deleteToken) {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        if (TextUtils.isEmpty(deleteToken)) {
            source.setResult(null);
            return source.getTask();
        }
        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/delete_by_token";
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", deleteToken)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        executorService.execute(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error al eliminar imagen anterior: " + response.code());
                }
                source.setResult(null);
            } catch (IOException e) {
                source.setException(e);
            }
        });
        return source.getTask();
    }

    private RequestBody buildStreamRequestBody(ContentResolver resolver, Uri uri, String mimeType) {
        MediaType mediaType = MediaType.parse(mimeType != null ? mimeType : "application/octet-stream");
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(@NonNull okio.BufferedSink sink) throws IOException {
                InputStream inputStream = null;
                try {
                    inputStream = resolver.openInputStream(uri);
                } catch (SecurityException se) {
                    throw new IOException("No hay permiso para acceder al archivo seleccionado", se);
                }
                if (inputStream == null) {
                    throw new IOException("No fue posible abrir el archivo seleccionado");
                }
                try (InputStream in = inputStream) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        sink.write(buffer, 0, read);
                    }
                }
            }
        };
    }

    private String obtenerExtension(Uri uri, @Nullable String mimeType) {
        if (mimeType != null) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                return "." + extension;
            }
        }
        String path = uri.getPath();
        if (path != null) {
            int lastDot = path.lastIndexOf('.');
            if (lastDot >= 0) {
                return path.substring(lastDot);
            }
        }
        return ".jpg";
    }
}