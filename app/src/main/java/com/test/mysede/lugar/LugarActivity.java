package com.test.mysede.lugar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.LugarDAO;
import com.test.mysede.R;
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

        btnNuevoLugar.setOnClickListener(v -> mostrarDialogoAgregar());

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
                    String nombre = etNombre.getText().toString().trim();
                    String cupoStr = etCupo.getText().toString().trim();
                    Lugar.Tipo tipo = (Lugar.Tipo) spTipo.getSelectedItem();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Integer cupo = parseCupo(cupoStr);
                    if (!cupoStr.isEmpty() && cupo == null) {
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
                    String nuevoNombre = etNombre.getText().toString().trim();
                    String nuevoCupoStr = etCupo.getText().toString().trim();
                    Lugar.Tipo nuevoTipo = (Lugar.Tipo) spTipo.getSelectedItem();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Integer nuevoCupo = parseCupo(nuevoCupoStr);
                    if (!nuevoCupoStr.isEmpty() && nuevoCupo == null) {
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
}
