package com.test.mysede;

import android.content.Intent;
import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.test.mysede.actividades.ListarActividadesActivity;
import com.test.mysede.citas.CrearCitaActivity;

import com.test.mysede.mantenedores.mantenedoresActivity;

import com.test.mysede.oferente.OferenteActivity;
import com.test.mysede.proyecto.ProyectoActivity;
import com.test.mysede.socio.SocioComunitarioActivity;
import com.test.mysede.tipoactividad.TipoActividadActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.mysede.lugar.LugarActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        Log.d("Firebase", " Firebase inicializado correctamente");

        // === Prueba de conexión y subida a Firebase Storage ===
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Creamos una referencia llamada "test_upload.txt"
        StorageReference pruebaRef = storageRef.child("test_upload.txt");

        try {
            // Crear archivo temporal para subir
            File tempFile = File.createTempFile("test_upload", ".txt", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write("Hola Firebase desde MySede".getBytes());
            fos.close();

            Uri fileUri = Uri.fromFile(tempFile);
            UploadTask uploadTask = pruebaRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "Archivo subido correctamente ", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "Archivo subido correctamente a Firebase Storage");
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir archivo : " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Firebase", "Error al subir archivo", e);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creando archivo temporal ", Toast.LENGTH_LONG).show();
        }

        // === Toolbar ===
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        // === Botones principales ===
        MaterialButton crearCitaButton = findViewById(R.id.btn_ir_crear_cita);
        MaterialButton reagendarCitaButton = findViewById(R.id.btn_ir_reagendar_cita);
        MaterialButton cancelarCitaButton = findViewById(R.id.btn_ir_cancelar_cita);
        MaterialButton actividadesButton = findViewById(R.id.btn_ir_actividades);

        MaterialButton btnMantenedores = findViewById(R.id.btn_ir_mantenedores);
        MaterialButton calendarioButton = findViewById(R.id.btn_ir_calendario);


        // === Acciones de los botones ===
        crearCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, CrearCitaActivity.class))
        );

        View.OnClickListener calendarioListener = v ->
                startActivity(new Intent(this, CalendarActivity.class));
        reagendarCitaButton.setOnClickListener(calendarioListener);
        cancelarCitaButton.setOnClickListener(calendarioListener);
        actividadesButton.setOnClickListener(v ->
                startActivity(new Intent(this, ListarActividadesActivity.class))
        );

        btnMantenedores.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, com.test.mysede.mantenedores.mantenedoresActivity.class))
        );


        // Botón para ir a los mantenedores
        Button btnmantenedores = findViewById(R.id.btn_ir_mantenedores);
        btnMantenedores.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, mantenedoresActivity.class);
            startActivity(intent);
        });


// Botón de calendario
        MaterialButton calendarioButton = findViewById(R.id.btn_ir_calendario);
        calendarioButton.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class))
        );

        calendarioButton.setOnClickListener(calendarioListener);

    }
}