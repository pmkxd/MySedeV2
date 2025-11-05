package com.test.mysede;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.material.button.MaterialButtonToggleGroup;
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
import com.test.mysede.actividades.ListarActividadesActivity;
import com.test.mysede.citas.CrearCitaActivity;
import com.test.mysede.mantenedores.mantenedoresActivity;
import com.test.mysede.usuarios.GestionUsuariosActivity;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.auth.SelectorRolActivity;
import com.test.mysede.model.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ====== SESIÓN Y PERMISOS ======
        sessionManager = new SessionManager(this);

        if (!sessionManager.haySesionActiva()) {
            startActivity(new Intent(this, SelectorRolActivity.class));
            finish();
            return;
        }

        Usuario usuario = sessionManager.obtenerUsuarioSesion();
        if (usuario != null) {
            PermissionManager.setUsuarioActual(usuario);
            Log.d("Roles", "Usuario: " + usuario.getNombre() + " - Rol: " + usuario.getRol().getNombreCompleto());
        } else {
            startActivity(new Intent(this, SelectorRolActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);
        probarFirebaseStorage();
        configurarToolbar();
        configurarBotones();
    }

    // ====== PRUEBA DE FIREBASE STORAGE ======
    private void probarFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference pruebaRef = storageRef.child("test_upload.txt");

        try {
            File tempFile = File.createTempFile("test_upload", ".txt", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write("Hola Firebase desde MySede".getBytes());
            fos.close();

            UploadTask uploadTask = pruebaRef.putFile(android.net.Uri.fromFile(tempFile));

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "Archivo subido correctamente");
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir archivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Firebase", "Error al subir archivo", e);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creando archivo temporal", Toast.LENGTH_LONG).show();
        }
    }

    // ====== TOOLBAR ======
    private void configurarToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_cambiar_rol) {
                cerrarSesion();
                return true;
            } else if (item.getItemId() == R.id.action_perfil) {
                mostrarInfoUsuario();
                return true;
            }
            return false;
        });
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        PermissionManager.setUsuarioActual(null);
        Intent intent = new Intent(this, SelectorRolActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    private void mostrarInfoUsuario() {
        Usuario usuario = sessionManager.obtenerUsuarioSesion();
        if (usuario != null) {
            Toast.makeText(this, "Usuario: " + usuario.getNombre() +
                    "\nRol: " + usuario.getRol().getNombreCompleto(), Toast.LENGTH_LONG).show();
        }
    }

    // ====== BOTONES CON VALIDACIÓN DE PERMISOS ======
    private void configurarBotones() {
        MaterialButton btnCrearCita = findViewById(R.id.btn_ir_crear_cita);
        if (btnCrearCita != null) {
            if (PermissionManager.tienePermiso(Permiso.CREAR_CITA))
                btnCrearCita.setOnClickListener(v -> startActivity(new Intent(this, CrearCitaActivity.class)));
            else hideButtonAndCard(btnCrearCita, R.id.card_crear_cita);
        }

        // ============================================
        // CARD DE CALENDARIO (CONSOLIDADA)
        // ============================================
        MaterialButton calendarioButton = findViewById(R.id.btn_ir_calendario);
        MaterialButtonToggleGroup calendarModeToggle = findViewById(R.id.calendar_mode_toggle);
        if (calendarModeToggle != null) {
            calendarModeToggle.check(R.id.calendar_mode_week);
        }
        MaterialButton reagendarCitaButton = findViewById(R.id.btn_ir_reagendar_cita);
        MaterialButton cancelarCitaButton = findViewById(R.id.btn_ir_cancelar_cita);

        View.OnClickListener calendarioListener = v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            if (calendarModeToggle != null) {
                int checkedId = calendarModeToggle.getCheckedButtonId();
                String mode = checkedId == R.id.calendar_mode_month
                        ? CalendarActivity.MODE_MONTH
                        : CalendarActivity.MODE_WEEK;
                intent.putExtra(CalendarActivity.EXTRA_INITIAL_MODE, mode);
            }
            startActivity(intent);
        };

        // Validar permisos para ver calendario
        if (PermissionManager.tienePermiso(Permiso.VER_CALENDARIO)) {
            // Botón principal de calendario (siempre visible si tiene permiso)
            if (calendarioButton != null) {
                calendarioButton.setOnClickListener(calendarioListener);
            }

            // Botón Reagendar (solo si tiene permiso de editar)
            if (reagendarCitaButton != null) {
                if (PermissionManager.tienePermiso(Permiso.EDITAR_CITA)) {
                    reagendarCitaButton.setVisibility(View.VISIBLE);
                    reagendarCitaButton.setOnClickListener(calendarioListener);
                } else {
                    reagendarCitaButton.setVisibility(View.GONE);
                }
            }

            // Botón Cancelar (solo si tiene permiso de eliminar)
            if (cancelarCitaButton != null) {
                if (PermissionManager.tienePermiso(Permiso.ELIMINAR_CITA)) {
                    cancelarCitaButton.setVisibility(View.VISIBLE);
                    cancelarCitaButton.setOnClickListener(calendarioListener);
                } else {
                    cancelarCitaButton.setVisibility(View.GONE);
                }
            }
        } else {
            // Ocultar toda la card de calendario si no tiene permiso
            View cardCalendario = findViewById(R.id.card_calendario);
            if (cardCalendario != null) {
                cardCalendario.setVisibility(View.GONE);
            }
        }

        MaterialButton btnActividades = findViewById(R.id.btn_ir_actividades);
        if (btnActividades != null) {
            if (PermissionManager.tienePermiso(Permiso.VER_ACTIVIDADES))
                btnActividades.setOnClickListener(v -> startActivity(new Intent(this, ListarActividadesActivity.class)));
            else hideButtonAndCard(btnActividades, R.id.card_gestion_actividades);
        }

        MaterialButton btnMantenedores = findViewById(R.id.btn_ir_mantenedores);
        if (btnMantenedores != null) {
            if (PermissionManager.esAdministrador())
                btnMantenedores.setOnClickListener(v -> startActivity(new Intent(this, mantenedoresActivity.class)));
            else hideButtonAndCard(btnMantenedores, R.id.card_mantenedores);
        }

        MaterialButton btnGestionUsuarios = findViewById(R.id.btn_ir_gestion_usuarios);
        if (btnGestionUsuarios != null) {
            if (PermissionManager.esAdministrador())
                btnGestionUsuarios.setOnClickListener(v -> startActivity(new Intent(this, GestionUsuariosActivity.class)));
            else hideButtonAndCard(btnGestionUsuarios, R.id.card_gestion_usuarios);
        }
    }

    private void hideButtonAndCard(View button, int cardId) {
        button.setVisibility(View.GONE);
        View card = findViewById(cardId);
        if (card != null) card.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Usuario usuario = sessionManager.obtenerUsuarioSesion();
        if (usuario != null)
            PermissionManager.setUsuarioActual(usuario);
    }
}
