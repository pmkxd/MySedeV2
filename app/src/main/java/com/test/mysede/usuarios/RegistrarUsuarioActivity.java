package com.test.mysede.usuarios;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PlantillaPermisos;
import com.test.mysede.auth.Rol;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity para registrar usuarios con Firebase Authentication y Firestore
 * Solo accesible por administradores
 */
public class RegistrarUsuarioActivity extends AppCompatActivity {

    private static final String TAG = "RegistrarUsuario";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private TextInputEditText etNombre, etEmail, etRut, etPassword, etConfirmPassword;
    private Spinner spinnerRol;
    private LinearLayout layoutPermisos;
    private Button btnGuardar, btnCargarPlantilla;
    private List<CheckBox> checkboxesPermisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        // Verificar permisos de administrador
        if (!PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            Toast.makeText(this, "No tienes permiso para crear usuarios", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configurar toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Nuevo Usuario");
        }

        inicializarVistas();
        configurarSpinnerRoles();
        generarCheckboxesPermisos();
        configurarEventos();
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombreUsuario);
        etEmail = findViewById(R.id.etEmailUsuario);
        etRut = findViewById(R.id.etRutUsuario);
        etPassword = findViewById(R.id.etPasswordUsuario);
        etConfirmPassword = findViewById(R.id.etConfirmPasswordUsuario);
        spinnerRol = findViewById(R.id.spinnerRolUsuario);
        layoutPermisos = findViewById(R.id.layoutPermisos);
        btnGuardar = findViewById(R.id.btnGuardarUsuario);
        btnCargarPlantilla = findViewById(R.id.btnCargarPlantilla);

        checkboxesPermisos = new ArrayList<>();
    }

    private void configurarSpinnerRoles() {
        List<String> roles = new ArrayList<>();
        for (Rol rol : Rol.values()) {
            roles.add(rol.getNombreCompleto());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);
    }

    private void generarCheckboxesPermisos() {
        layoutPermisos.removeAllViews();
        checkboxesPermisos.clear();

        // Agrupar permisos por categoría
        for (Permiso.Categoria categoria : Permiso.Categoria.values()) {
            // Header de categoría
            android.widget.TextView tvCategoria = new android.widget.TextView(this);
            tvCategoria.setText(categoria.getNombre());
            tvCategoria.setTextSize(16);
            tvCategoria.setTextColor(getColor(R.color.md_theme_primary));
            tvCategoria.setTextAppearance(android.R.style.TextAppearance_Material_Body2);
            LinearLayout.LayoutParams paramsCategoria = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            paramsCategoria.setMargins(0, 24, 0, 8);
            tvCategoria.setLayoutParams(paramsCategoria);
            layoutPermisos.addView(tvCategoria);

            // Checkboxes de permisos
            for (Permiso permiso : Permiso.values()) {
                if (permiso.getCategoria() == categoria) {
                    CheckBox checkbox = new CheckBox(this);
                    checkbox.setText(permiso.getDescripcion());
                    checkbox.setTag(permiso);
                    checkbox.setTextSize(14);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(16, 4, 0, 4);
                    checkbox.setLayoutParams(params);

                    layoutPermisos.addView(checkbox);
                    checkboxesPermisos.add(checkbox);
                }
            }
        }
    }

    private void configurarEventos() {
        // Botón cargar plantilla
        btnCargarPlantilla.setOnClickListener(v -> cargarPlantillaPermisos());

        // Botón guardar
        btnGuardar.setOnClickListener(v -> registrarUsuario());
    }

    private void cargarPlantillaPermisos() {
        String rolSeleccionado = spinnerRol.getSelectedItem().toString();
        Rol rol = Rol.fromNombreCompleto(rolSeleccionado);

        if (rol != null) {
            Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);

            // Desmarcar todos
            for (CheckBox checkbox : checkboxesPermisos) {
                checkbox.setChecked(false);
            }

            // Marcar los de la plantilla
            for (CheckBox checkbox : checkboxesPermisos) {
                Permiso permiso = (Permiso) checkbox.getTag();
                if (permisos.contains(permiso)) {
                    checkbox.setChecked(true);
                }
            }

            Toast.makeText(this, "Plantilla de " + rol.getNombreCompleto() + " cargada", Toast.LENGTH_SHORT).show();
        }
    }

    private void registrarUsuario() {
        // Validar campos
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String rut = etRut.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese el nombre");
            etNombre.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Ingrese el email");
            etEmail.requestFocus();
            return;
        }

        if (!email.contains("@")) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingrese la contraseña");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }

        // Obtener rol
        String rolSeleccionado = spinnerRol.getSelectedItem().toString();
        Rol rol = Rol.fromNombreCompleto(rolSeleccionado);

        // Obtener permisos seleccionados
        Set<Permiso> permisos = new HashSet<>();
        for (CheckBox checkbox : checkboxesPermisos) {
            if (checkbox.isChecked()) {
                permisos.add((Permiso) checkbox.getTag());
            }
        }

        if (permisos.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar al menos un permiso", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Registrando...");

        // Crear usuario en Firebase Authentication
        crearUsuarioFirebaseAuth(nombre, email, rut, password, rol, permisos);
    }

    private void crearUsuarioFirebaseAuth(String nombre, String email, String rut,
                                          String password, Rol rol, Set<Permiso> permisos) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario creado en Authentication
                        String uid = task.getResult().getUser().getUid();
                        Log.d(TAG, "Usuario creado en Authentication con UID: " + uid);

                        // Ahora guardar datos en Firestore
                        guardarUsuarioFirestore(uid, nombre, email, rut, rol, permisos);
                    } else {
                        // Error al crear usuario
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("GUARDAR USUARIO");

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this,
                                    "Este email ya está registrado",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Error al registrar: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error al crear usuario", task.getException());
                        }
                    }
                });
    }

    private void guardarUsuarioFirestore(String uid, String nombre, String email, String rut,
                                         Rol rol, Set<Permiso> permisos) {
        // Convertir permisos a lista de strings
        List<String> permisosString = new ArrayList<>();
        for (Permiso permiso : permisos) {
            permisosString.add(permiso.name());
        }

        // Crear mapa de datos
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("nombre", nombre);
        usuarioData.put("email", email);
        usuarioData.put("rut", rut);
        usuarioData.put("rol", rol.name());
        usuarioData.put("permisos", permisosString);
        usuarioData.put("activo", true);
        usuarioData.put("fechaCreacion", System.currentTimeMillis());
        usuarioData.put("ultimoAcceso", System.currentTimeMillis());

        // Guardar en Firestore
        db.collection("usuarios")
                .document(uid)
                .set(usuarioData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario guardado en Firestore");
                    Toast.makeText(this,
                            "Usuario registrado exitosamente",
                            Toast.LENGTH_LONG).show();

                    // Cerrar sesión temporal si se creó con la cuenta actual
                    // (Firebase cambia automáticamente al nuevo usuario)
                    mAuth.signOut();

                    finish();
                })
                .addOnFailureListener(e -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("GUARDAR USUARIO");

                    Toast.makeText(this,
                            "Error al guardar datos: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al guardar en Firestore", e);

                    // Eliminar usuario de Authentication si falló Firestore
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
