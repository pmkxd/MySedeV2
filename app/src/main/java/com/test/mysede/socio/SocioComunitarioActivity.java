package com.test.mysede.socio;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.test.mysede.ui.SystemBarsHelper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.SocioComunitarioDAO;
import com.test.mysede.R;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.model.SocioComunitario;

import java.util.ArrayList;
import java.util.List;

public class SocioComunitarioActivity extends AppCompatActivity {

    private RecyclerView recyclerSocios;
    private SocioComunitarioAdapter adapter;
    private List<SocioComunitario> listaSocios;
    private FloatingActionButton btnNuevoSocio;
    private SocioComunitarioDAO socioComunitarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socio_comunitario);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerSocios = findViewById(R.id.recyclerSocios);
        btnNuevoSocio = findViewById(R.id.btnNuevoSocio);
        socioComunitarioDAO = new SocioComunitarioDAO();
        listaSocios = new ArrayList<>();

        adapter = new SocioComunitarioAdapter(this, listaSocios, new SocioComunitarioAdapter.OnItemClickListener() {
            @Override
            public void onVer(SocioComunitario socio) {
                mostrarDetalles(socio);
            }

            @Override
            public void onEditar(SocioComunitario socio, int position) {
                mostrarDialogoEditar(socio, position);
            }

            @Override
            public void onEliminar(int position) {
                confirmarEliminar(position);
            }
        });

        recyclerSocios.setLayoutManager(new LinearLayoutManager(this));
        recyclerSocios.setAdapter(adapter);

        btnNuevoSocio.setOnClickListener(v -> {
            if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_SOCIOS)) {
                Toast.makeText(this, "No tienes permiso para crear socios comunitarios", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarDialogoAgregar();
        });

        cargarSocios();
    }

    private void cargarSocios() {
        socioComunitarioDAO.getAllSociosComunitarios(new SocioComunitarioDAO.OnSociosComunitariosLoadedListener() {
            @Override
            public void onSociosComunitariosLoaded(ArrayList<SocioComunitario> socios) {
                runOnUiThread(() -> {
                    listaSocios.clear();
                    listaSocios.addAll(socios);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(SocioComunitarioActivity.this,
                        "Error al cargar los socios comunitarios", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Ver detalles
    private void mostrarDetalles(SocioComunitario socio) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(socio.getNombre()).append("\n\nBeneficiarios:\n");
        if (socio.getBeneficiarios().isEmpty()) {
            sb.append("No tiene beneficiarios");
        } else {
            socio.getBeneficiarios().forEach(b -> sb.append("- ").append(b.getCaracterizacion()).append("\n"));
        }

        new AlertDialog.Builder(this)
                .setTitle("Detalles del Socio")
                .setMessage(sb.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // ➕ Agregar nuevo
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_socio, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreSocio);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Socio")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_SOCIOS)) {
                        Toast.makeText(this, "No tienes permiso para crear socios comunitarios", Toast.LENGTH_SHORT).show();
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
                    if (existeSocioConNombre(nombre)) {
                        Toast.makeText(this, "Ya existe un socio comunitario con este nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SocioComunitario nuevoSocio = new SocioComunitario(nombre);
                    socioComunitarioDAO.saveSocioComunitario(nuevoSocio, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(SocioComunitarioActivity.this, "Socio registrado", Toast.LENGTH_SHORT).show();
                                cargarSocios();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(SocioComunitarioActivity.this,
                                    "No fue posible registrar el socio", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ✏️ Editar
    private void mostrarDialogoEditar(SocioComunitario socio, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_socio, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreSocio);

        etNombre.setText(socio.getNombre());

        new AlertDialog.Builder(this)
                .setTitle("Editar Socio")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_SOCIOS)) {
                        Toast.makeText(this, "No tienes permiso para editar socios comunitarios", Toast.LENGTH_SHORT).show();
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
                    if (existeSocioConNombreExcepto(nuevoNombre, socio.getId())) {
                        Toast.makeText(this, "Ya existe un socio comunitario con este nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SocioComunitario socioActualizado = new SocioComunitario(nuevoNombre);
                    socioActualizado.setId(socio.getId());
                    socioComunitarioDAO.saveSocioComunitario(socioActualizado, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(SocioComunitarioActivity.this, "Socio actualizado", Toast.LENGTH_SHORT).show();
                                cargarSocios();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(SocioComunitarioActivity.this,
                                    "No fue posible actualizar el socio", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar
    private void confirmarEliminar(int position) {
        if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_SOCIOS)) {
            Toast.makeText(this, "No tienes permiso para eliminar socios comunitarios", Toast.LENGTH_SHORT).show();
            return;
        }
        SocioComunitario socio = listaSocios.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este socio?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    socioComunitarioDAO.deleteSocioComunitario(socio, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(SocioComunitarioActivity.this, "Socio eliminado", Toast.LENGTH_SHORT).show();
                                cargarSocios();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(SocioComunitarioActivity.this,
                                    "No fue posible eliminar el socio", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private boolean existeSocioConNombre(String nombre) {
        for (SocioComunitario s : listaSocios) {
            if (s.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeSocioConNombreExcepto(String nombre, String idExcluir) {
        for (SocioComunitario s : listaSocios) {
            if (s.getNombre().equalsIgnoreCase(nombre) && !s.getId().equals(idExcluir)) {
                return true;
            }
        }
        return false;
    }
}
