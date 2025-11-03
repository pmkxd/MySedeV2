package com.test.mysede.usuarios;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

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

    private TextInputEditText etNombreUsuario, etEmailUsuario, etPasswordUsuario;
    private Spinner spinnerRolUsuario;
    private LinearLayout layoutPermisos;
    private Button btnGuardarUsuario, btnCargarPlantilla;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<CheckBox> checkboxesPermisos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        if (!PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Crear Usuario");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupRoleSpinner();
        renderPermisosCheckboxes();
        setupListeners();
    }

    private void initViews() {
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etEmailUsuario = findViewById(R.id.etEmailUsuario);
        etPasswordUsuario = findViewById(R.id.etPasswordUsuario);
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
        btnCargarPlantilla.setOnClickListener(v -> {
            Rol rol = Rol.fromNombreCompleto(spinnerRolUsuario.getSelectedItem().toString());
            Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);

            for (CheckBox c : checkboxesPermisos) {
                c.setChecked(permisos.contains((Permiso) c.getTag()));
            }

            Toast.makeText(this, "Permisos cargados", Toast.LENGTH_SHORT).show();
        });

        btnGuardarUsuario.setOnClickListener(v -> crearUsuarioFirebase());
    }

    private void crearUsuarioFirebase() {
        String nombre = etNombreUsuario.getText().toString().trim();
        String email = etEmailUsuario.getText().toString().trim();
        String password = etPasswordUsuario.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    guardarUsuarioEnFirestore(uid, nombre, email);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error Auth: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void guardarUsuarioEnFirestore(String uid, String nombre, String email) {

        Rol rol = Rol.fromNombreCompleto(spinnerRolUsuario.getSelectedItem().toString());
        Set<Permiso> permisos = new HashSet<>();
        for (CheckBox c : checkboxesPermisos)
            if (c.isChecked()) permisos.add((Permiso) c.getTag());

        Usuario user = new Usuario(nombre, email, rol);
        user.setId(uid);
        user.setPermisos(permisos);

        Map<String, Object> data = new HashMap<>();
        data.put("id", uid);
        data.put("nombre", nombre);
        data.put("email", email);
        data.put("rol", rol.name());
        data.put("permisos", user.getPermisosComoLista());
        data.put("activo", true);
        data.put("fechaCreacion", user.getFechaCreacion());

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
