package com.test.mysede.DAO;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.model.ArchivoAdjunto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class ArchivoAdjuntoDAO {
    private static final String CLOUD_NAME = "dtzndokk9";
    public static final String PRESET_IMAGEN = "mysede_image_10mb";
    public static final String PRESET_VIDEO = "mysede_video_50mb";
    public static final String RESOURCE_IMAGE = "image";
    public static final String RESOURCE_VIDEO = "video";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ExecutorService uploadExecutor = Executors.newCachedThreadPool();

    // Guardar metadatos en Firestore
    public Task<DocumentReference> guardarArchivo(@NonNull ArchivoAdjunto archivo) {
        Map<String, Object> data = FirestoreModelMapper.archivoAdjuntoToMap(archivo);
        return db.collection("archivosAdjuntos").add(data);
    }

    public Task<ArchivoAdjunto> subirArchivoACloudinary(@NonNull Context context, @NonNull ArchivoAdjunto archivo) {
        TaskCompletionSource<ArchivoAdjunto> source = new TaskCompletionSource<>();
        Uri uri = archivo.getUri();
        if (uri == null) {
            source.setException(new IllegalArgumentException("El archivo no tiene una URI asociada"));
            return source.getTask();
        }
        String resourceType = archivo.getResourceType() != null ? archivo.getResourceType() : RESOURCE_IMAGE;
        String preset = archivo.getUploadPreset();
        if (preset == null) {
            preset = RESOURCE_IMAGE.equals(resourceType) ? PRESET_IMAGEN : PRESET_VIDEO;
        }
        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/" + resourceType + "/upload";
        String mimeType = archivo.getTipo() != null ? archivo.getTipo() : "application/octet-stream";

        RequestBody fileBody = buildStreamRequestBody(context.getContentResolver(), uri, mimeType);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", preset)
                .addFormDataPart("file", archivo.getNombre(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        uploadExecutor.execute(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error al subir archivo: " + response.code());
                }
                if (response.body() == null) {
                    throw new IOException("Respuesta vacía del servidor de archivos");
                }
                JSONObject json = new JSONObject(response.body().string());
                String secureUrl = json.optString("secure_url", null);
                String publicId = json.optString("public_id", null);
                archivo.setUrl(secureUrl);
                archivo.setCloudinaryPublicId(publicId);
                archivo.setUri(null);
                source.setResult(archivo);
            } catch (IOException | JSONException e) {
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
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                try (InputStream inputStream = resolver.openInputStream(uri)) {
                    if (inputStream == null) {
                        throw new IOException("No fue posible abrir el archivo seleccionado");
                    }
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        sink.write(buffer, 0, read);
                    }
                }
            }
        };
    }
}
