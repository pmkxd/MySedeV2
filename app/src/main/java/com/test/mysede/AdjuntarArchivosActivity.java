package com.test.mysede;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.test.mysede.model.ArchivoAdjunto;

import java.util.ArrayList;

public class AdjuntarArchivosActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 123;
    private MaterialButton btnSeleccionarArchivo, btnGuardarArchivo;
    private LinearLayout layoutArchivosAdjuntos;
    private ArrayList<ArchivoAdjunto> archivosSeleccionados = new ArrayList<>();

    // Selector moderno de archivos
    private final ActivityResultLauncher<Intent> selectorArchivosLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            ArchivoAdjunto archivo = obtenerDetallesArchivo(uri);
                            archivosSeleccionados.add(archivo);
                        }
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        ArchivoAdjunto archivo = obtenerDetallesArchivo(uri);
                        archivosSeleccionados.add(archivo);
                    }
                    actualizarVistaDeArchivosAdjuntos();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjuntar_archivos);

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

        // Botón: guardar
        btnGuardarArchivo.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("archivosAdjuntos", archivosSeleccionados);
            setResult(RESULT_OK, resultIntent);
            finish(); // Cierra la pantalla y vuelve a CrearActividadActivity
        });

    }

    private void abrirSelectorDeArchivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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

    @SuppressLint("Range")
    private ArchivoAdjunto obtenerDetallesArchivo(Uri uri) {
        String nombre = "Archivo desconocido";
        String tipo = getContentResolver().getType(uri);
        long tamaño = 0;
        String url = "?";


        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex != -1) nombre = cursor.getString(nameIndex);
                if (sizeIndex != -1) tamaño = cursor.getLong(sizeIndex);
            }
        }
        return new ArchivoAdjunto(nombre, tipo, tamaño, uri, null);
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                ArchivoAdjunto archivo = obtenerDetallesArchivo(uri);
                archivosSeleccionados.add(archivo);
                actualizarVistaDeArchivosAdjuntos();
            }
        }
    }
}
