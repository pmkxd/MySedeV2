package com.test.mysede.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.MainActivity;
import com.test.mysede.R;
import com.test.mysede.model.Usuario;
import com.test.mysede.usuarios.UsuarioAdapter;
import com.test.mysede.usuarios.UsuarioHelper;

import java.util.List;

/**
 * Activity temporal para seleccionar un rol y probar el sistema de permisos
 * Esta pantalla se eliminará cuando se implemente Firebase Authentication
 */
public class SelectorRolActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsuarios;
    private UsuarioAdapter adapter;
    private TextView tvTitulo, tvDescripcion;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_rol);

        sessionManager = new SessionManager(this);

        // Inicializar vistas
        tvTitulo = findViewById(R.id.tvTituloSelector);
        tvDescripcion = findViewById(R.id.tvDescripcionSelector);
        recyclerViewUsuarios = findViewById(R.id.recyclerViewUsuarios);

        // Configurar RecyclerView
        recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(this));

        // Obtener usuarios de prueba
        List<Usuario> usuarios = UsuarioHelper.obtenerUsuariosPrueba();

        // Configurar adaptador con listener de click
        adapter = new UsuarioAdapter(this, usuarios, (usuario, posicion) -> {
            // Al seleccionar un usuario, crear sesión e ir a MainActivity
            seleccionarUsuario(usuario);
        });
        recyclerViewUsuarios.setAdapter(adapter);
    }

    private void seleccionarUsuario(Usuario usuario) {
        // Crear sesión
        sessionManager.crearSesion(usuario);

        // Ir a MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // No permitir volver atrás desde esta pantalla
        // El usuario debe seleccionar un rol
    }
}