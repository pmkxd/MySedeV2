package com.test.mysede.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.model.Usuario;

import java.util.List;

/**
 * Activity para gestionar usuarios del sistema
 * Solo accesible por usuarios con rol Administrador
 */
public class GestionUsuariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsuarioAdapter adapter;
    private FloatingActionButton fabCrear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // Verificar permisos
        if (!PermissionManager.tienePermiso(Permiso.VER_USUARIOS)) {
            Toast.makeText(this, "No tienes permiso para acceder a esta sección", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cargar usuarios
        cargarUsuarios();

        // Configurar FAB - Mostrar opciones
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
        List<Usuario> usuarios = UsuarioHelper.obtenerUsuariosPrueba();

        adapter = new UsuarioAdapter(this, usuarios, (usuario, posicion) -> {
            mostrarOpcionesUsuario(usuario, posicion);
        });

        recyclerView.setAdapter(adapter);
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
                            verDetalles(usuario, posicion);
                            break;
                        case "Editar":
                            editarUsuario(usuario, posicion);
                            break;
                        case "Eliminar":
                            confirmarEliminar(usuario, posicion);
                            break;
                    }
                })
                .show();
    }

    private void verDetalles(Usuario usuario, int posicion) {
        Intent intent = new Intent(this, DetalleUsuarioActivity.class);
        intent.putExtra("posicion", posicion);
        startActivity(intent);
    }

    private void editarUsuario(Usuario usuario, int posicion) {
        Intent intent = new Intent(this, CrearUsuarioActivity.class);
        intent.putExtra("posicion", posicion);
        intent.putExtra("modo", "editar");
        startActivity(intent);
    }

    private void confirmarEliminar(Usuario usuario, int posicion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Está seguro que desea eliminar a " + usuario.getNombre() + "?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    UsuarioHelper.eliminarUsuario(posicion);
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    cargarUsuarios();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
