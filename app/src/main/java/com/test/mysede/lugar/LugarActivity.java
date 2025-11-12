package com.test.mysede.lugar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.test.mysede.ui.SystemBarsHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.LugarDAO;
import com.test.mysede.R;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.model.Lugar;

import java.util.ArrayList;
import java.util.List;

public class LugarActivity extends AppCompatActivity {

    private RecyclerView recyclerLugares;
    private LugarAdapter adapter;
    private List<Lugar> listaLugares;
    private FloatingActionButton btnNuevoLugar;
    private LugarDAO lugarDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lugar);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerLugares = findViewById(R.id.recyclerLugares);
        btnNuevoLugar = findViewById(R.id.btnNuevoLugar);

        lugarDAO = new LugarDAO();
        listaLugares = new ArrayList<>();


        adapter = new LugarAdapter(this, listaLugares, new LugarAdapter.OnItemClickListener() {
            @Override
            public void onVer(Lugar lugar) {
                mostrarDetalles(lugar);
            }

            @Override
            public void onEditar(Lugar lugar, int position) {
                mostrarDialogoEditar(lugar, position);
            }

            @Override
            public void onEliminar(int position) {
                confirmarEliminar(position);
            }
        });

        recyclerLugares.setLayoutManager(new LinearLayoutManager(this));
        recyclerLugares.setAdapter(adapter);

        btnNuevoLugar.setOnClickListener(v -> {
            if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_LUGARES)) {
                Toast.makeText(this, "No tienes permiso para crear lugares", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarDialogoAgregar();
        });

        cargarLugares();
    }

    private void cargarLugares() {
        lugarDAO.getAllLugares(new LugarDAO.OnLugaresLoadedListener() {
            @Override
            public void onLugaresLoaded(ArrayList<Lugar> lugares) {
                runOnUiThread(() -> {
                    listaLugares.clear();
                    listaLugares.addAll(lugares);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(LugarActivity.this,
                        "Error al cargar los lugares", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 👁️ Ver detalles
    private void mostrarDetalles(Lugar lugar) {
        new AlertDialog.Builder(this)
                .setTitle("Detalles del Lugar")
                .setMessage("Nombre: " + lugar.getNombre() +
                        "\nTipo: " + lugar.getTipo().toString().replace("_", " ") +
                        "\nCupo: " + (lugar.getCupo().isPresent() ? lugar.getCupo().get() : "No especificado"))
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // ➕ Agregar nuevo lugar
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lugar, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreLugar);
        EditText etCupo = dialogView.findViewById(R.id.etCupoLugar);
        Spinner spTipo = dialogView.findViewById(R.id.spTipoLugar);

        ArrayAdapter<Lugar.Tipo> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Lugar.Tipo.values());
        spTipo.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Lugar")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_LUGARES)) {
                        Toast.makeText(this, "No tienes permiso para crear lugares", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nombre = etNombre.getText().toString().trim();
                    String cupoStr = etCupo.getText().toString().trim();
                    Lugar.Tipo tipo = (Lugar.Tipo) spTipo.getSelectedItem();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nombre.length() < 3) {
                        Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validar duplicados
                    if (existeLugarConNombre(nombre)) {
                        Toast.makeText(this, "Ya existe un lugar con este nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Integer cupo = parseCupo(cupoStr);
                    if (!cupoStr.isEmpty() && cupo == null) {
                        return;
                    }

                    // Validar cupo > 0 y <= 1000
                    if (cupo != null && cupo <= 0) {
                        Toast.makeText(this, "El cupo debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (cupo != null && cupo > 1000) {
                        Toast.makeText(this, "El cupo no puede ser mayor a 1000", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Lugar nuevoLugar = new Lugar(nombre, tipo, cupo);
                    lugarDAO.saveLugar(nuevoLugar, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(LugarActivity.this, "Lugar registrado", Toast.LENGTH_SHORT).show();
                                cargarLugares();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(LugarActivity.this,
                                    "No fue posible registrar el lugar", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ✏️ Editar lugar
    private void mostrarDialogoEditar(Lugar lugar, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lugar, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreLugar);
        EditText etCupo = dialogView.findViewById(R.id.etCupoLugar);
        Spinner spTipo = dialogView.findViewById(R.id.spTipoLugar);

        etNombre.setText(lugar.getNombre());
        etCupo.setText(lugar.getCupo().isPresent() ? lugar.getCupo().get().toString() : "");

        ArrayAdapter<Lugar.Tipo> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Lugar.Tipo.values());
        spTipo.setAdapter(spinnerAdapter);
        spTipo.setSelection(lugar.getTipo().ordinal());

        new AlertDialog.Builder(this)
                .setTitle("Editar Lugar")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_LUGARES)) {
                        Toast.makeText(this, "No tienes permiso para editar lugares", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nuevoNombre = etNombre.getText().toString().trim();
                    String nuevoCupoStr = etCupo.getText().toString().trim();
                    Lugar.Tipo nuevoTipo = (Lugar.Tipo) spTipo.getSelectedItem();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nuevoNombre.length() < 3) {
                        Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validar duplicados (excluir el lugar actual)
                    if (existeLugarConNombreExcepto(nuevoNombre, lugar.getId())) {
                        Toast.makeText(this, "Ya existe un lugar con este nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Integer nuevoCupo = parseCupo(nuevoCupoStr);
                    if (!nuevoCupoStr.isEmpty() && nuevoCupo == null) {
                        return;
                    }

                    // Validar cupo > 0 y <= 1000
                    if (nuevoCupo != null && nuevoCupo <= 0) {
                        Toast.makeText(this, "El cupo debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nuevoCupo != null && nuevoCupo > 1000) {
                        Toast.makeText(this, "El cupo no puede ser mayor a 1000", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Lugar lugarActualizado = new Lugar(nuevoNombre, nuevoTipo, nuevoCupo);
                    lugarActualizado.setId(lugar.getId());
                    lugarDAO.saveLugar(lugarActualizado, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(LugarActivity.this, "Lugar actualizado", Toast.LENGTH_SHORT).show();
                                cargarLugares();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(LugarActivity.this,
                                    "No fue posible actualizar el lugar", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar lugar
    private void confirmarEliminar(int position) {
        if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_LUGARES)) {
            Toast.makeText(this, "No tienes permiso para eliminar lugares", Toast.LENGTH_SHORT).show();
            return;
        }
        Lugar lugar = listaLugares.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este lugar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    lugarDAO.deleteLugar(lugar, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(LugarActivity.this, "Lugar eliminado", Toast.LENGTH_SHORT).show();
                                cargarLugares();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(LugarActivity.this,
                                    "No fue posible eliminar el lugar", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private Integer parseCupo(String cupoStr) {
        if (cupoStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(cupoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cupo inválido", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Verifica si ya existe un lugar con el nombre dado
     */
    private boolean existeLugarConNombre(String nombre) {
        for (Lugar l : listaLugares) {
            if (l.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si ya existe un lugar con el nombre dado, excluyendo un ID específico
     */
    private boolean existeLugarConNombreExcepto(String nombre, String idExcluir) {
        for (Lugar l : listaLugares) {
            if (l.getNombre().equalsIgnoreCase(nombre) && !l.getId().equals(idExcluir)) {
                return true;
            }
        }
        return false;
    }
}
