package com.test.mysede.usuarios;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PlantillaPermisos;
import com.test.mysede.auth.Rol;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity para crear o editar usuarios
 */
public class CrearUsuarioActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail;
    private Spinner spinnerRol;
    private LinearLayout layoutPermisos;
    private Button btnGuardar, btnCargarPlantilla;

    private boolean modoEditar = false;
    private int posicion = -1;
    private Usuario usuarioEditar;

    private List<CheckBox> checkboxesPermisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        // Verificar permisos
        if (!PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            Toast.makeText(this, "No tienes permiso para crear usuarios", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verificar modo
        modoEditar = "editar".equals(getIntent().getStringExtra("modo"));
        posicion = getIntent().getIntExtra("posicion", -1);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(modoEditar ? "Editar Usuario" : "Crear Usuario");
        }

        inicializarVistas();
        configurarSpinnerRoles();
        generarCheckboxesPermisos();
        configurarEventos();

        if (modoEditar && posicion != -1) {
            usuarioEditar = UsuarioHelper.obtenerUsuarioPorIndice(posicion);
            cargarDatosUsuario();
        }
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombreUsuario);
        etEmail = findViewById(R.id.etEmailUsuario);
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
            // Agregar header de categoría
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

            // Agregar checkboxes de permisos de esta categoría
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
        btnGuardar.setOnClickListener(v -> guardarUsuario());
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

    private void cargarDatosUsuario() {
        if (usuarioEditar == null) return;

        etNombre.setText(usuarioEditar.getNombre());
        etEmail.setText(usuarioEditar.getEmail());

        // Seleccionar rol
        String rolNombre = usuarioEditar.getRol().getNombreCompleto();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerRol.getAdapter();
        int posicionRol = adapter.getPosition(rolNombre);
        spinnerRol.setSelection(posicionRol);

        // Marcar permisos
        Set<Permiso> permisosUsuario = usuarioEditar.getPermisos();
        for (CheckBox checkbox : checkboxesPermisos) {
            Permiso permiso = (Permiso) checkbox.getTag();
            checkbox.setChecked(permisosUsuario.contains(permiso));
        }
    }

    private void guardarUsuario() {
        // Validar campos
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese el email del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Ingrese un email válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener rol seleccionado
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

        // Crear o actualizar usuario
        Usuario usuario;
        if (modoEditar) {
            usuario = usuarioEditar;
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setRol(rol);
            usuario.setPermisos(permisos);
            UsuarioHelper.actualizarUsuario(posicion, usuario);
            Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            usuario = new Usuario(nombre, email, rol);
            usuario.setPermisos(permisos);
            UsuarioHelper.agregarUsuario(usuario);
            Toast.makeText(this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}