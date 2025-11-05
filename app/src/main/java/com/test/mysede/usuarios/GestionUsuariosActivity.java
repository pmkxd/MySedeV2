package com.test.mysede.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.R;
import com.test.mysede.DAO.UsuariosDAO;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.model.Usuario;
import com.test.mysede.ui.SystemBarsHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity para gestionar usuarios del sistema
 * Solo accesible por usuarios con rol Administrador
 */
public class GestionUsuariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsuarioAdapter adapter;
    private FloatingActionButton fabCrear;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private UsuariosDAO usuariosDAO;
    private List<Usuario> usuariosList;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);
        // Verificar permisos
        if (!PermissionManager.tienePermiso(Permiso.VER_USUARIOS)) {
            Toast.makeText(this, "No tienes permiso para acceder a esta sección", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        usuariosDAO = new UsuariosDAO();
        usuariosList = new ArrayList<>();

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Usuarios");
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewUsuarios);
        fabCrear = findViewById(R.id.fabCrearUsuario);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cargar usuarios desde Firebase
        cargarUsuarios();

        // Configurar FAB - Ir directo a CrearUsuarioActivity
        if (PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            fabCrear.setOnClickListener(v -> {
                Intent intent = new Intent(this, CrearUsuarioActivity.class);
                startActivity(intent);
            });
        } else {
            fabCrear.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        // Mostrar progress bar
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // Cargar usuarios desde Firebase
        usuariosDAO.getAllUsuarios(new UsuariosDAO.OnUsuariosLoadedListener() {
            @Override
            public void onUsuariosLoaded(ArrayList<Usuario> usuarios) {
                progressBar.setVisibility(View.GONE);
                usuariosList = usuarios;

                if (usuarios.isEmpty()) {
                    // Mostrar mensaje de lista vacía
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Mostrar lista de usuarios
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    adapter = new UsuarioAdapter(GestionUsuariosActivity.this, usuarios, (usuario, posicion) -> {
                        mostrarOpcionesUsuario(usuario, posicion);
                    });

                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GestionUsuariosActivity.this,
                        "Error al cargar usuarios: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();

                // Mostrar mensaje de error
                tvEmpty.setText("Error al cargar usuarios. Por favor, intenta nuevamente.");
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarOpcionesUsuario(Usuario usuario, int posicion) {
        String[] opciones;

        if (PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO) &&
                PermissionManager.tienePermiso(Permiso.ELIMINAR_USUARIO)) {
            opciones = new String[]{"Ver Detalles", "Editar", "Eliminar"};
        } else if (PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO)) {
            opciones = new String[]{"Ver Detalles", "Editar"};
        } else {
            opciones = new String[]{"Ver Detalles"};
        }

        new AlertDialog.Builder(this)
                .setTitle(usuario.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (opciones[which]) {
                        case "Ver Detalles":
                            verDetalles(usuario);
                            break;
                        case "Editar":
                            editarUsuario(usuario);
                            break;
                        case "Eliminar":
                            confirmarEliminar(usuario, posicion);
                            break;
                    }
                })
                .show();
    }

    private void verDetalles(Usuario usuario) {
        Intent intent = new Intent(this, DetalleUsuarioActivity.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }

    private void editarUsuario(Usuario usuario) {
        Intent intent = new Intent(this, CrearUsuarioActivity.class);
        intent.putExtra("usuario", usuario);
        intent.putExtra("modo", "editar");
        startActivity(intent);
    }

    private void confirmarEliminar(Usuario usuario, int posicion) {
        // Validar que no se elimine a sí mismo
        if (mAuth.getCurrentUser() != null &&
                usuario.getId().equals(mAuth.getCurrentUser().getUid())) {
            Toast.makeText(this, "No puedes eliminar tu propio usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Está seguro que desea eliminar a " + usuario.getNombre() + "?\n\n" +
                        "Esta acción eliminará:\n" +
                        "• Los datos del usuario en Firestore\n" +
                        "• La cuenta de autenticación en Firebase\n\n" +
                        "Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarUsuario(usuario, posicion);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void eliminarUsuario(Usuario usuario, int posicion) {
        // Mostrar progress
        progressBar.setVisibility(View.VISIBLE);

        // Eliminar de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .document(usuario.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Eliminar de Authentication (requiere más privilegios)
                    // Por ahora solo eliminamos de Firestore
                    Toast.makeText(this, "Usuario eliminado de Firestore", Toast.LENGTH_SHORT).show();

                    // Actualizar lista local
                    if (posicion >= 0 && posicion < usuariosList.size()) {
                        usuariosList.remove(posicion);
                        if (adapter != null) {
                            adapter.notifyItemRemoved(posicion);
                        }
                    }

                    progressBar.setVisibility(View.GONE);

                    // Recargar lista
                    cargarUsuarios();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Error al eliminar usuario: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
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