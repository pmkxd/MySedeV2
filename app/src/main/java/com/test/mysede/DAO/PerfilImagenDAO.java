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

    // Mismos valores que ya usas
    private static final String CLOUD_NAME = "dgnbyuqyd";
    private static final String RESOURCE_TYPE = "image";
    private static final String UPLOAD_PRESET = "mysede_avatar";
    // El folder ahora lo maneja el preset en Cloudinary
    // private static final String AVATAR_FOLDER = "mysede/avatars";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Sube el avatar a Cloudinary usando el upload preset unsigned "mysede_avatar",
     * siguiendo el mismo patrón que ArchivoAdjuntoDAO.subirArchivoACloudinary.
     */
    public Task<PerfilImagenResultado> subirAvatar(@NonNull Context context, @NonNull Uri imagenUri) {
        TaskCompletionSource<PerfilImagenResultado> source = new TaskCompletionSource<>();

        // mimeType opcional, cae en octet-stream si viene null
        String mimeType = context.getContentResolver().getType(imagenUri);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        RequestBody fileBody =
                buildStreamRequestBody(context.getContentResolver(), imagenUri, mimeType);

        // Mismo esquema que ArchivoAdjuntoDAO: https://api.cloudinary.com/v1_1/<cloud_name>/<resource_type>/upload
        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/" + RESOURCE_TYPE + "/upload";
        String fileName = "avatar_" + System.currentTimeMillis() + obtenerExtension(imagenUri, mimeType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                // El folder y el delete token se configuran en el preset
                .addFormDataPart("file", fileName, fileBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        executorService.execute(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                String bodyString = response.body() != null ? response.body().string() : null;

                if (!response.isSuccessful()) {
                    String detalle = bodyString != null ? (" - " + bodyString) : "";
                    throw new IOException("Error al subir imagen: " + response.code() + detalle);
                }
                if (bodyString == null) {
                    throw new IOException("Respuesta vacía del servidor de archivos");
                }

                JSONObject json = new JSONObject(bodyString);
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

    /**
     * Elimina un avatar anterior usando delete_token, igual que antes pero
     * con la misma estructura de ejecución que el upload.
     */
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

    /**
     * Igual idea que en ArchivoAdjuntoDAO: stream del contentResolver hacia OkHttp.
     */
    private RequestBody buildStreamRequestBody(ContentResolver resolver, Uri uri, String mimeType) {
        MediaType mediaType = MediaType.parse(
                mimeType != null ? mimeType : "application/octet-stream"
        );
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                InputStream inputStream = null;
                try {
                    inputStream = resolver.openInputStream(uri);
                } catch (SecurityException se) {
                    throw new IOException(
                            "No hay permiso para acceder al archivo seleccionado. " +
                                    "Si elegiste el archivo con ACTION_OPEN_DOCUMENT, " +
                                    "asegúrate de haber concedido permisos persistentes.",
                            se
                    );
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

    /**
     * Obtiene una extensión razonable para el archivo (para el nombre que se ve en Cloudinary).
     */
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
        // fallback
        return ".jpg";
    }
}
