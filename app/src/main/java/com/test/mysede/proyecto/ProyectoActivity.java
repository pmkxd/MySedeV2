package com.test.mysede.proyecto;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.ProyectoDAO;
import com.test.mysede.R;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.model.Proyecto;
import com.test.mysede.ui.SystemBarsHelper;
import java.util.ArrayList;
import java.util.List;

public class ProyectoActivity extends AppCompatActivity {

    private RecyclerView recyclerProyectos;
    private ProyectoAdapter adapter;
    private List<Proyecto> listaProyectos;
    private FloatingActionButton btnNuevoProyecto;
    private ProyectoDAO proyectoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyecto);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerProyectos = findViewById(R.id.recyclerProyectos);
        btnNuevoProyecto = findViewById(R.id.btnNuevoProyecto);

        proyectoDAO = new ProyectoDAO();
        listaProyectos = new ArrayList<>();

        adapter = new ProyectoAdapter(this, listaProyectos, new ProyectoAdapter.OnItemClickListener() {
            @Override
            public void onVer(Proyecto proyecto) {
                mostrarDetalles(proyecto);
            }

            @Override
            public void onEditar(Proyecto proyecto, int position) {
                mostrarDialogoEditar(proyecto, position);
            }

            @Override
            public void onEliminar(int position) {
                confirmarEliminar(position);
            }
        });

        recyclerProyectos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProyectos.setAdapter(adapter);

        btnNuevoProyecto.setOnClickListener(v -> {
            if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_PROYECTOS)) {
                Toast.makeText(this, "No tienes permiso para crear proyectos", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarDialogoAgregar();
        });
        cargarProyectos();
    }

    private void cargarProyectos() {
        proyectoDAO.getAllProyectos(new ProyectoDAO.OnProyectosLoadedListener() {
            @Override
            public void onProyectosLoaded(ArrayList<Proyecto> proyectos) {
                runOnUiThread(() -> {
                    listaProyectos.clear();
                    listaProyectos.addAll(proyectos);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(ProyectoActivity.this,
                        "Error al cargar los proyectos", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 👁️ Ver detalles
    private void mostrarDetalles(Proyecto proyecto) {
        new AlertDialog.Builder(this)
                .setTitle("Detalles del Proyecto")
                .setMessage("Nombre: " + proyecto.getNombre())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // ➕ Agregar nuevo
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_proyecto, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreProyecto);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Proyecto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_PROYECTOS)) {
                        Toast.makeText(this, "No tienes permiso para crear proyectos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nombre.length() < 3) {
                        Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Validar duplicados
                    if (existeProyectoConNombre(nombre)) {
                        Toast.makeText(this, "Ya existe un proyecto con este nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Proyecto nuevoProyecto = new Proyecto(nombre);
                    proyectoDAO.saveProyecto(nuevoProyecto, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(ProyectoActivity.this, "Proyecto registrado", Toast.LENGTH_SHORT).show();
                                cargarProyectos();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(ProyectoActivity.this,
                                    "No fue posible registrar el proyecto", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ✏️ Editar
    private void mostrarDialogoEditar(Proyecto proyecto, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_proyecto, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreProyecto);
        etNombre.setText(proyecto.getNombre());

        new AlertDialog.Builder(this)
                .setTitle("Editar Proyecto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_PROYECTOS)) {
                        Toast.makeText(this, "No tienes permiso para editar proyectos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nuevoNombre = etNombre.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nuevoNombre.length() < 3) {
                        Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Validar duplicados (excluir el proyecto actual)
                    if (existeProyectoConNombreExcepto(nuevoNombre, proyecto.getId())) {
                        Toast.makeText(this, "Ya existe un proyecto con este nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Proyecto proyectoActualizado = new Proyecto(nuevoNombre);
                    proyectoActualizado.setId(proyecto.getId());
                    proyectoDAO.saveProyecto(proyectoActualizado, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(ProyectoActivity.this, "Proyecto actualizado", Toast.LENGTH_SHORT).show();
                                cargarProyectos();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(ProyectoActivity.this,
                                    "No fue posible actualizar el proyecto", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar
    private void confirmarEliminar(int position) {
        if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_PROYECTOS)) {
            Toast.makeText(this, "No tienes permiso para eliminar proyectos", Toast.LENGTH_SHORT).show();
            return;
        }
        Proyecto proyecto = listaProyectos.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este proyecto?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    proyectoDAO.deleteProyecto(proyecto, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(ProyectoActivity.this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                                cargarProyectos();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(ProyectoActivity.this,
                                    "No fue posible eliminar el proyecto", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Verifica si ya existe un proyecto con el nombre dado
     */
    private boolean existeProyectoConNombre(String nombre) {
        for (Proyecto p : listaProyectos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si ya existe un proyecto con el nombre dado, excluyendo un ID específico
     */
    private boolean existeProyectoConNombreExcepto(String nombre, String idExcluir) {
        for (Proyecto p : listaProyectos) {
            if (p.getNombre().equalsIgnoreCase(nombre) && !p.getId().equals(idExcluir)) {
                return true;
            }
        }
        return false;
    }
}
