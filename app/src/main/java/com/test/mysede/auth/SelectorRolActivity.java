package com.test.mysede.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.DAO.UsuariosDAO;
import com.test.mysede.MainActivity;
import com.test.mysede.R;
import com.test.mysede.model.Usuario;
import com.test.mysede.usuarios.UsuarioAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity temporal para seleccionar un usuario y probar el sistema de permisos
 * Carga usuarios reales desde Firebase
 * Esta pantalla es útil para desarrollo y pruebas
 */
public class SelectorRolActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsuarios;
    private UsuarioAdapter adapter;
    private TextView tvTitulo, tvDescripcion, tvEmpty;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private UsuariosDAO usuariosDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_rol);

        sessionManager = new SessionManager(this);
        usuariosDAO = new UsuariosDAO();

        // Inicializar vistas
        tvTitulo = findViewById(R.id.tvTituloSelector);
        tvDescripcion = findViewById(R.id.tvDescripcionSelector);
        recyclerViewUsuarios = findViewById(R.id.recyclerViewUsuarios);
        progressBar = findViewById(R.id.progressBarSelector);
        tvEmpty = findViewById(R.id.tvEmptySelector);

        // Configurar RecyclerView
        recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(this));

        // Cargar usuarios desde Firebase
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        // Mostrar progress bar
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewUsuarios.setVisibility(View.GONE);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }

        // Cargar usuarios desde Firebase
        usuariosDAO.getAllUsuarios(new UsuariosDAO.OnUsuariosLoadedListener() {
            @Override
            public void onUsuariosLoaded(ArrayList<Usuario> usuarios) {
                progressBar.setVisibility(View.GONE);

                if (usuarios.isEmpty()) {
                    // Mostrar mensaje de lista vacía
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No hay usuarios disponibles.\n\nPor favor, crea un usuario primero.");
                    }
                    recyclerViewUsuarios.setVisibility(View.GONE);
                } else {
                    // Mostrar lista de usuarios
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(View.GONE);
                    }
                    recyclerViewUsuarios.setVisibility(View.VISIBLE);

                    // Configurar adaptador con listener de click
                    adapter = new UsuarioAdapter(SelectorRolActivity.this, usuarios, (usuario, posicion) -> {
                        // Al seleccionar un usuario, crear sesión e ir a MainActivity
                        seleccionarUsuario(usuario);
                    });
                    recyclerViewUsuarios.setAdapter(adapter);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SelectorRolActivity.this,
                        "Error al cargar usuarios: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();

                // Mostrar mensaje de error
                if (tvEmpty != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error al cargar usuarios.\n\nVerifica tu conexión a Firebase.");
                }
                recyclerViewUsuarios.setVisibility(View.GONE);
            }
        });
    }

    private void seleccionarUsuario(Usuario usuario) {
        // Validar que el usuario esté activo
        if (!usuario.isActivo()) {
            Toast.makeText(this, "Este usuario está inactivo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear sesión
        sessionManager.crearSesion(usuario);

        Toast.makeText(this, "Sesión iniciada como " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

        // Ir a MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // No permitir volver atrás desde esta pantalla
        // El usuario debe seleccionar un rol o cerrar la app
    }
}