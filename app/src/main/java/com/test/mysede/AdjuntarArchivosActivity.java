package com.test.mysede;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdjuntarArchivosActivity extends AppCompatActivity {

    private Spinner spinner;
    private MaterialButton btnSeleccionarArchivo, btnGuardarArchivo;
    private ImageView imagePreview, btnEliminarImagen;
    private Uri imagenSeleccionadaUri;
    private StorageReference storageReference;
    FrameLayout contenedorImagen = findViewById(R.id.contenedorImagen);

    // Lanzador moderno para abrir galería
    private final ActivityResultLauncher<Intent> selectorImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenSeleccionadaUri = result.getData().getData();
                    imagePreview.setImageURI(imagenSeleccionadaUri);
                    imagePreview.setVisibility(View.VISIBLE);
                    btnEliminarImagen.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjuntar_archivos);

        // Referencias UI
        spinner = findViewById(R.id.spinner);
        btnSeleccionarArchivo = findViewById(R.id.btnSeleccionarArchivo);
        btnGuardarArchivo = findViewById(R.id.btnGuardarArchivo);
        imagePreview = new ImageView(this);
        btnEliminarImagen = new ImageView(this);
        imagePreview = findViewById(R.id.imagePreview);
        btnEliminarImagen = findViewById(R.id.btnEliminarImagen);

        // Referencia Firebase
        storageReference = FirebaseStorage.getInstance().getReference("imagenes_adjuntas");

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // Acción de volver
            // startActivity(new Intent(this, OtraActivity.class));
            finish();
        });

        // Seleccionar archivo
        btnSeleccionarArchivo.setOnClickListener(v -> abrirGaleria());

        // Eliminar imagen
        btnEliminarImagen.setOnClickListener(v -> eliminarImagenSeleccionada());

        // Guardar archivo
        btnGuardarArchivo.setOnClickListener(v -> guardarImagenEnFirebase());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectorImagenLauncher.launch(intent);
        contenedorImagen.setVisibility(View.VISIBLE);
    }

    private void eliminarImagenSeleccionada() {
        imagenSeleccionadaUri = null;
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
        btnEliminarImagen.setVisibility(View.GONE);
        Toast.makeText(this, "Imagen eliminada", Toast.LENGTH_SHORT).show();
        contenedorImagen.setVisibility(View.GONE);

    }

    private void guardarImagenEnFirebase() {
        if (imagenSeleccionadaUri == null) {
            Toast.makeText(this, "Primero selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombreArchivo = System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageReference.child(nombreArchivo);

        UploadTask uploadTask = fileRef.putFile(imagenSeleccionadaUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show();

            // Volver a la actividad anterior
            // Intent intent = new Intent(this, TuActivityDestino.class);
            // startActivity(intent);
            finish();

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
