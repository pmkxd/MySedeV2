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
import com.test.mysede.lugar.LugarActivity;

// ============================================
// IMPORTS DEL SISTEMA DE ROLES
// ============================================
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.auth.SelectorRolActivity;
import com.test.mysede.model.Usuario;
import com.test.mysede.usuarios.GestionUsuariosActivity;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ============================================
        // PASO 1: VERIFICAR SESIÓN (SISTEMA DE ROLES)
        // ============================================
        sessionManager = new SessionManager(this);

        // Verificar si hay sesión activa
        if (!sessionManager.haySesionActiva()) {
            // No hay sesión, ir a selector de rol
            Intent intent = new Intent(this, SelectorRolActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Cargar usuario de la sesión
        Usuario usuario = sessionManager.obtenerUsuarioSesion();
        if (usuario != null) {
            PermissionManager.setUsuarioActual(usuario);
            Log.d("Roles", "Usuario cargado: " + usuario.getNombre() + " - Rol: " + usuario.getRol().getNombreCompleto());
        } else {
            // Si no se pudo cargar el usuario, ir a selector
            Intent intent = new Intent(this, SelectorRolActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // FIN VERIFICACIÓN DE SESIÓN

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        Log.d("Firebase", "Firebase inicializado correctamente");

        // === Prueba de conexión y subida a Firebase Storage ===
        probarFirebaseStorage();

        // === Configurar UI ===
        configurarToolbar();
        configurarBotones();
    }

    /**
     * Prueba de Firebase Storage
     */
    private void probarFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference pruebaRef = storageRef.child("test_upload.txt");

        try {
            File tempFile = File.createTempFile("test_upload", ".txt", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write("Hola Firebase desde MySede".getBytes());
            fos.close();

            Uri fileUri = Uri.fromFile(tempFile);
            UploadTask uploadTask = pruebaRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "Archivo subido correctamente a Firebase Storage");
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir archivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Firebase", "Error al subir archivo", e);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creando archivo temporal", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Configurar Toolbar con menú de opciones
     */
    private void configurarToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        // Configurar menú del toolbar
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

        // Inflar el menú
        toolbar.inflateMenu(R.menu.menu_main);
    }

    /**
     * Cerrar sesión y volver a SelectorRolActivity
     */
    private void cerrarSesion() {
        // Limpiar sesión
        sessionManager.cerrarSesion();

        // Limpiar permisos
        PermissionManager.setUsuarioActual(null);

        // Ir a SelectorRolActivity
        Intent intent = new Intent(this, SelectorRolActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    /**
     * Mostrar información del usuario actual
     */
    private void mostrarInfoUsuario() {
        Usuario usuario = sessionManager.obtenerUsuarioSesion();
        if (usuario != null) {
            String mensaje = "Usuario: " + usuario.getNombre() + "\nRol: " + usuario.getRol().getNombreCompleto();
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Configurar todos los botones con validación de permisos
     */
    private void configurarBotones() {
        // ============================================
        // BOTONES DE CITAS (CON VALIDACIÓN DE PERMISOS)
        // ============================================

        // Botón Crear Cita
        MaterialButton crearCitaButton = findViewById(R.id.btn_ir_crear_cita);
        if (crearCitaButton != null) {
            if (PermissionManager.tienePermiso(Permiso.CREAR_CITA)) {
                crearCitaButton.setOnClickListener(v ->
                        startActivity(new Intent(this, CrearCitaActivity.class))
                );
            } else {
                crearCitaButton.setVisibility(View.GONE);
                findViewById(R.id.card_crear_cita).setVisibility(View.GONE);
            }
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

        // ============================================
        // BOTÓN DE ACTIVIDADES (CON VALIDACIÓN DE PERMISOS)
        // ============================================
        MaterialButton actividadesButton = findViewById(R.id.btn_ir_actividades);
        if (actividadesButton != null) {
            if (PermissionManager.tienePermiso(Permiso.VER_ACTIVIDADES)) {
                actividadesButton.setOnClickListener(v ->
                        startActivity(new Intent(this, ListarActividadesActivity.class))
                );
            } else {
                actividadesButton.setVisibility(View.GONE);
                findViewById(R.id.card_gestion_actividades).setVisibility(View.GONE);
            }
        }

        // ============================================
        // BOTÓN DE MANTENEDORES (SOLO ADMINISTRADOR)
        // ============================================
        MaterialButton btnMantenedores = findViewById(R.id.btn_ir_mantenedores);
        if (btnMantenedores != null) {
            if (PermissionManager.esAdministrador()) {
                btnMantenedores.setOnClickListener(v ->
                        startActivity(new Intent(this, mantenedoresActivity.class))
                );
            } else {
                btnMantenedores.setVisibility(View.GONE);
                View cardMantenedores = findViewById(R.id.card_mantenedores);
                if (cardMantenedores != null) {
                    cardMantenedores.setVisibility(View.GONE);
                }
            }
        }

        // ============================================
        // BOTÓN DE GESTIÓN DE USUARIOS (SOLO ADMINISTRADOR)
        // ============================================
        MaterialButton btnGestionUsuarios = findViewById(R.id.btn_ir_gestion_usuarios);
        if (btnGestionUsuarios != null) {
            if (PermissionManager.esAdministrador()) {
                btnGestionUsuarios.setOnClickListener(v ->
                        startActivity(new Intent(this, GestionUsuariosActivity.class))
                );
            } else {
                btnGestionUsuarios.setVisibility(View.GONE);
                View cardUsuarios = findViewById(R.id.card_gestion_usuarios);
                if (cardUsuarios != null) {
                    cardUsuarios.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar permisos al volver a la activity
        if (sessionManager.haySesionActiva()) {
            Usuario usuario = sessionManager.obtenerUsuarioSesion();
            if (usuario != null) {
                PermissionManager.setUsuarioActual(usuario);
            }
        }
    }
}