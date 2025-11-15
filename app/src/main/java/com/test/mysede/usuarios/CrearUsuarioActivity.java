package com.test.mysede.usuarios;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.test.mysede.ui.SystemBarsHelper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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

public class CrearUsuarioActivity extends AppCompatActivity {

    private TextInputEditText etNombreUsuario, etEmailUsuario, etPasswordUsuario, etRutUsuario;
    private Spinner spinnerRolUsuario;
    private LinearLayout layoutPermisos;
    private Button btnGuardarUsuario, btnCargarPlantilla;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<CheckBox> checkboxesPermisos = new ArrayList<>();

    // Variables para modo edición
    private boolean modoEdicion = false;
    private Usuario usuarioAEditar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        // Detectar modo edición
        String modo = getIntent().getStringExtra("modo");
        modoEdicion = "editar".equals(modo);
        if (modoEdicion) {
            usuarioAEditar = (Usuario) getIntent().getSerializableExtra("usuario");
            if (usuarioAEditar == null) {
                Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Verificar permisos
        if (!modoEdicion && !PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (modoEdicion && !PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO)) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(modoEdicion ? "Editar Usuario" : "Crear Usuario");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupRoleSpinner();
        ocultarSeccionPermisos(); // Ocultar checkboxes de permisos
        setupListeners();

        // Si es modo edición, precargar datos
        if (modoEdicion) {
            precargarDatosUsuario();
        }
    }

    private void initViews() {
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etEmailUsuario = findViewById(R.id.etEmailUsuario);
        etPasswordUsuario = findViewById(R.id.etPasswordUsuario);
        etRutUsuario = findViewById(R.id.etRutUsuario); // ✅ Nuevo campo RUT
        spinnerRolUsuario = findViewById(R.id.spinnerRolUsuario);
        layoutPermisos = findViewById(R.id.layoutPermisos);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        btnCargarPlantilla = findViewById(R.id.btnCargarPlantilla);
    }

    private void setupRoleSpinner() {
        List<String> roles = new ArrayList<>();
        for (Rol r : Rol.values()) roles.add(r.getNombreCompleto());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRolUsuario.setAdapter(adapter);
    }

    private void ocultarSeccionPermisos() {
        // Ocultar el botón de cargar plantilla (ya no se usa)
        if (btnCargarPlantilla != null) {
            btnCargarPlantilla.setVisibility(android.view.View.GONE);
        }

        // Ocultar el contenedor de permisos (ya no se editan manualmente)
        if (layoutPermisos != null) {
            layoutPermisos.setVisibility(android.view.View.GONE);
        }
    }

    private void renderPermisosCheckboxes() {
        layoutPermisos.removeAllViews();
        checkboxesPermisos.clear();

        for (Permiso.Categoria categoria : Permiso.Categoria.values()) {
            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(categoria.getNombre());
            tv.setTextSize(16);
            tv.setPadding(0, 16, 0, 8);
            layoutPermisos.addView(tv);

            for (Permiso p : Permiso.values()) {
                if (p.getCategoria() == categoria) {
                    CheckBox chk = new CheckBox(this);
                    chk.setText(p.getDescripcion());
                    chk.setTag(p);
                    layoutPermisos.addView(chk);
                    checkboxesPermisos.add(chk);
                }
            }
        }
    }

    private void setupListeners() {
        // Listener para el botón guardar/actualizar
        btnGuardarUsuario.setOnClickListener(v -> crearUsuarioFirebase());
    }

    private void crearUsuarioFirebase() {
        String nombre = etNombreUsuario.getText().toString().trim();
        String email = etEmailUsuario.getText().toString().trim();
        String password = etPasswordUsuario.getText().toString().trim();
        String rut = etRutUsuario.getText().toString().trim();

        // Validaciones básicas
        if (nombre.isEmpty() || rut.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar longitud mínima del nombre
        if (nombre.length() < 3) {
            Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de RUT chileno
        if (!validarRutChileno(rut)) {
            Toast.makeText(this, "El RUT ingresado no es válido (formato: 12345678-9)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (modoEdicion) {
            // Modo edición: actualizar usuario existente
            // Validar contraseña solo si se ingresó una nueva
            if (!password.isEmpty() && password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            actualizarUsuarioFirebase(nombre, rut, password);
        } else {
            // Modo creación: validar email y password
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar formato de email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "El email ingresado no es válido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar longitud mínima de contraseña
            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear nuevo usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();
                        guardarUsuarioEnFirestore(uid, nombre, email, rut, password);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error Auth: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }

    private void guardarUsuarioEnFirestore(String uid, String nombre, String email, String rut, String pass) {

        Rol rol = Rol.fromNombreCompleto(spinnerRolUsuario.getSelectedItem().toString());

        // Asignar permisos automáticamente según el rol seleccionado
        Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);

        Usuario user = new Usuario(nombre, email, rol);
        user.setId(uid);
        user.setPermisos(permisos);

        Map<String, Object> data = new HashMap<>();
        data.put("id", uid);
        data.put("nombre", nombre);
        data.put("email", email);
        data.put("rut", rut); // ✅ Guardando RUT
        data.put("rol", rol.name());
        data.put("permisos", user.getPermisosComoLista());
        data.put("activo", true);
        data.put("fechaCreacion", user.getFechaCreacion());
        data.put("pass", pass);
        data.put("profileImageUrl", Usuario.DEFAULT_PROFILE_IMAGE_URL);
        data.put("profileImagePublicId", Usuario.DEFAULT_PROFILE_IMAGE_PUBLIC_ID);
        data.put("profileImageDeleteToken", null);
        db.collection("usuarios")
                .document(uid)
                .set(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Usuario creado ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void actualizarUsuarioFirebase(String nombre, String rut, String password) {
        if (usuarioAEditar == null) return;

        String uid = usuarioAEditar.getId();
        Rol rol = Rol.fromNombreCompleto(spinnerRolUsuario.getSelectedItem().toString());

        // Asignar permisos automáticamente según el rol seleccionado
        Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);

        // Crear objeto Usuario temporal para usar getPermisosComoLista
        Usuario userTemp = new Usuario(nombre, usuarioAEditar.getEmail(), rol);
        userTemp.setPermisos(permisos);

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("rut", rut);
        updates.put("rol", rol.name());
        updates.put("permisos", userTemp.getPermisosComoLista());

        // Si se proporcionó una nueva contraseña, actualizarla también
        if (!password.isEmpty()) {
            updates.put("pass", password);
        }

        db.collection("usuarios")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Usuario actualizado ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void precargarDatosUsuario() {
        if (usuarioAEditar == null) return;

        // Precargar nombre
        etNombreUsuario.setText(usuarioAEditar.getNombre());

        // Precargar email (y deshabilitarlo, no se puede cambiar)
        etEmailUsuario.setText(usuarioAEditar.getEmail());
        etEmailUsuario.setEnabled(false);
        etEmailUsuario.setFocusable(false);

        // Precargar RUT
        if (usuarioAEditar.getRut() != null) {
            etRutUsuario.setText(usuarioAEditar.getRut());
        }

        // Password vacío (opcional en edición)
        etPasswordUsuario.setHint("Dejar vacío para mantener contraseña actual");

        // Seleccionar rol (los permisos se asignarán automáticamente según el rol)
        if (usuarioAEditar.getRol() != null) {
            String nombreRol = usuarioAEditar.getRol().getNombreCompleto();
            for (int i = 0; i < spinnerRolUsuario.getCount(); i++) {
                if (spinnerRolUsuario.getItemAtPosition(i).toString().equals(nombreRol)) {
                    spinnerRolUsuario.setSelection(i);
                    break;
                }
            }
        }

        // Cambiar texto del botón
        btnGuardarUsuario.setText("Actualizar Usuario");
    }

    /**
     * Valida el formato y dígito verificador de un RUT chileno
     * Formato esperado: 12345678-9 o 12345678-K
     */
    private boolean validarRutChileno(String rut) {
        // Validar formato básico (números-dígito)
        if (!rut.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;
        }

        // Separar número y dígito verificador
        String[] partes = rut.split("-");
        String numero = partes[0];
        String dvIngresado = partes[1].toUpperCase();

        // Calcular dígito verificador
        int suma = 0;
        int multiplicador = 2;

        // Recorrer de derecha a izquierda
        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dvCalculado = 11 - resto;
        String dvEsperado;

        if (dvCalculado == 11) {
            dvEsperado = "0";
        } else if (dvCalculado == 10) {
            dvEsperado = "K";
        } else {
            dvEsperado = String.valueOf(dvCalculado);
        }

        return dvEsperado.equals(dvIngresado);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
