package com.test.mysede;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.MimeTypeMap;
import com.test.mysede.ui.SystemBarsHelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.test.mysede.DAO.ArchivoAdjuntoDAO;
import com.test.mysede.model.ArchivoAdjunto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdjuntarArchivosActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 123;
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;
    private static final List<String> IMAGE_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp", "image/avif");
    private static final List<String> VIDEO_MIME_TYPES = Arrays.asList("video/mp4", "video/quicktime", "video/webm");
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "avif");
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList("mp4", "mov", "webm");
    private MaterialButton btnSeleccionarArchivo, btnGuardarArchivo;
    private LinearLayout layoutArchivosAdjuntos;
    private ArrayList<ArchivoAdjunto> archivosSeleccionados = new ArrayList<>();

    // Selector moderno de archivos
    private final ActivityResultLauncher<Intent> selectorArchivosLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // Cuando usamos ACTION_OPEN_DOCUMENT podemos persistir permisos
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            // Intent flags permitting persistable grants
                            try {
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (SecurityException ignored) {
                                // Algunos proveedores no soportan permisos persistentes; no fallamos por eso
                            }
                            ArchivoAdjunto archivo = obtenerDetallesArchivo(uri);
                            if (archivo != null) {
                                archivosSeleccionados.add(archivo);
                            }
                        }
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        try {
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException ignored) {
                            // Ignore if provider doesn't support persistable grants
                        }
                        ArchivoAdjunto archivo = obtenerDetallesArchivo(uri);
                        if (archivo != null) {
                            archivosSeleccionados.add(archivo);
                        }
                    }
                    actualizarVistaDeArchivosAdjuntos();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjuntar_archivos);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        // Inicializar vistas
        btnSeleccionarArchivo = findViewById(R.id.btnSeleccionarArchivo);
        btnGuardarArchivo = findViewById(R.id.btnGuardarArchivo);
        layoutArchivosAdjuntos = findViewById(R.id.layoutArchivosAdjuntos);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Botón: seleccionar archivos
        btnSeleccionarArchivo.setOnClickListener(v -> abrirSelectorDeArchivos());
        ArrayList<ArchivoAdjunto> existentes = getIntent().getParcelableArrayListExtra("archivosAdjuntos");
        if (existentes != null && !existentes.isEmpty()) {
            archivosSeleccionados.addAll(existentes);
            actualizarVistaDeArchivosAdjuntos();
        }
        // Botón: guardar
        btnGuardarArchivo.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("archivosAdjuntos", archivosSeleccionados);
            setResult(RESULT_OK, resultIntent);
            finish(); // Cierra la pantalla y vuelve a CrearActividadActivity
        });

    }

    private void abrirSelectorDeArchivos() {
        // Usar Storage Access Framework (ACTION_OPEN_DOCUMENT) para poder persistir permisos
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // Pedimos permiso de lectura y que el permiso pueda persistir
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        selectorArchivosLauncher.launch(Intent.createChooser(intent, "Selecciona los archivos"));
    }


    private void actualizarVistaDeArchivosAdjuntos() {
        layoutArchivosAdjuntos.removeAllViews();

        if (archivosSeleccionados.isEmpty()) {
            layoutArchivosAdjuntos.setVisibility(View.GONE);
            return;
        }

        layoutArchivosAdjuntos.setVisibility(View.VISIBLE);

        for (int i = 0; i < archivosSeleccionados.size(); i++) {
            ArchivoAdjunto archivo = archivosSeleccionados.get(i);

            CardView card = new CardView(this);
            card.setRadius(16);
            card.setCardElevation(6);
            card.setUseCompatPadding(true);

            LinearLayout contenedor = new LinearLayout(this);
            contenedor.setOrientation(LinearLayout.VERTICAL);
            contenedor.setPadding(24, 16, 24, 16);

            TextView nombre = new TextView(this);
            nombre.setText("📄 " + archivo.getNombre());

            TextView tipo = new TextView(this);
            tipo.setText("Tipo: " + archivo.getTipo());

            TextView tamaño = new TextView(this);
            tamaño.setText("Tamaño: " + (archivo.getTamaño() / 1024) + " KB");

            // Botones para renombrar y eliminar
            LinearLayout acciones = new LinearLayout(this);
            acciones.setOrientation(LinearLayout.HORIZONTAL);
            acciones.setPadding(0, 8, 0, 0);

            Button btnRenombrar = new Button(this);
            btnRenombrar.setText("Renombrar");
            btnRenombrar.setTextSize(12);

            Button btnEliminar = new Button(this);
            btnEliminar.setText("Eliminar");
            btnEliminar.setTextSize(12);

            // Acción: renombrar
            btnRenombrar.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Renombrar archivo");

                final EditText input = new EditText(this);
                input.setText(archivo.getNombre());
                builder.setView(input);

                builder.setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Obtener la extensión del nombre original (por ejemplo, ".jpg")
                    String nombreOriginal = archivo.getNombre();
                    String extension = "";
                    int puntoIndex = nombreOriginal.lastIndexOf('.');
                    if (puntoIndex != -1) {
                        extension = nombreOriginal.substring(puntoIndex); // incluye el punto
                    }

                    // Eliminar extensión si el usuario la escribió (por error)
                    if (nuevoNombre.contains(".")) {
                        nuevoNombre = nuevoNombre.substring(0, nuevoNombre.lastIndexOf('.'));
                    }

                    // Volver a agregar la extensión original
                    String nombreFinal = nuevoNombre + extension;

                    archivo.setNombre(nombreFinal);
                    actualizarVistaDeArchivosAdjuntos();
                    Toast.makeText(this, "Archivo renombrado a: " + nombreFinal, Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
                builder.show();
            });

            // Acción: eliminar
            btnEliminar.setOnClickListener(v -> {
                archivosSeleccionados.remove(archivo);
                actualizarVistaDeArchivosAdjuntos();
            });

            // Añadir botones
            acciones.addView(btnRenombrar);
            acciones.addView(btnEliminar);

            contenedor.addView(nombre);
            contenedor.addView(tipo);
            contenedor.addView(tamaño);
            contenedor.addView(acciones);

            card.addView(contenedor);
            layoutArchivosAdjuntos.addView(card);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                ArchivoAdjunto archivo = obtenerDetallesArchivo(uri);
                if (archivo != null) {
                    archivosSeleccionados.add(archivo);
                    actualizarVistaDeArchivosAdjuntos();
                }
            }
        }
    }
    private boolean esImagenValida(String mimeType, String extension) {
        return IMAGE_MIME_TYPES.contains(mimeType) || IMAGE_EXTENSIONS.contains(extension);
    }

    private boolean esVideoValido(String mimeType, String extension) {
        return VIDEO_MIME_TYPES.contains(mimeType) || VIDEO_EXTENSIONS.contains(extension);
    }

    private long obtenerTamañoDesdeFuente(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return 0;
            }
            byte[] buffer = new byte[8192];
            long total = 0;
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                total += read;
            }
            return total;
        } catch (IOException e) {
            return 0;
        }
    }

    private String obtenerExtension(String nombre) {
        if (TextUtils.isEmpty(nombre)) {
            return "";
        }
        int puntoIndex = nombre.lastIndexOf('.');
        if (puntoIndex == -1 || puntoIndex == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(puntoIndex + 1).toLowerCase();
    }

    @SuppressLint("Range")
    private ArchivoAdjunto obtenerDetallesArchivo(Uri uri) {
        String nombre = "Archivo desconocido";
        String tipo = getContentResolver().getType(uri);
        long tamaño = 0;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex != -1) nombre = cursor.getString(nameIndex);
                if (sizeIndex != -1) tamaño = cursor.getLong(sizeIndex);
            }
        }
        if (tamaño <= 0) {
            tamaño = obtenerTamañoDesdeFuente(uri);
        }
        String extension = obtenerExtension(nombre);
        if (tipo == null && !TextUtils.isEmpty(extension)) {
            tipo = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        boolean esImagen = esImagenValida(tipo, extension);
        boolean esVideo = esVideoValido(tipo, extension);
        if (!esImagen && !esVideo) {
            Toast.makeText(this, "Formato no permitido", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (esImagen && tamaño > MAX_IMAGE_BYTES) {
            Toast.makeText(this, "Las imágenes deben pesar máximo 10 MB", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (esVideo && tamaño > MAX_VIDEO_BYTES) {
            Toast.makeText(this, "Los videos deben pesar máximo 50 MB", Toast.LENGTH_SHORT).show();
            return null;
        }

        String preset = esImagen ? ArchivoAdjuntoDAO.PRESET_IMAGEN : ArchivoAdjuntoDAO.PRESET_VIDEO;
        String resourceType = esImagen ? ArchivoAdjuntoDAO.RESOURCE_IMAGE : ArchivoAdjuntoDAO.RESOURCE_VIDEO;
        return new ArchivoAdjunto(nombre, tipo, tamaño, uri, null, resourceType, preset);
    }
}
