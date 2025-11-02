package com.test.mysede.tipoactividad;

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
import com.test.mysede.DAO.TipoActividadDAO;
import com.test.mysede.R;
import com.test.mysede.model.TipoActividad;

import java.util.ArrayList;
import java.util.List;

public class TipoActividadActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TipoActividadAdapter adapter;
    private List<TipoActividad> listaTipos;
    private FloatingActionButton btnNuevaActividad;
    private TipoActividadDAO tipoActividadDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_actividad);

        recyclerView = findViewById(R.id.recyclerTipos);
        btnNuevaActividad = findViewById(R.id.btnNuevaActividad);
        tipoActividadDAO = new TipoActividadDAO();
        listaTipos = new ArrayList<>();

        adapter = new TipoActividadAdapter(this, listaTipos, new TipoActividadAdapter.OnItemClickListener() {
            @Override
            public void onVer(TipoActividad tipo) {
                mostrarDetalles(tipo);
            }

            @Override
            public void onEditar(TipoActividad tipo, int position) {
                mostrarDialogoEditar(tipo, position);
            }

            @Override
            public void onEliminar(int position) {
                confirmarEliminar(position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnNuevaActividad.setOnClickListener(v -> mostrarDialogoAgregar());

        cargarTiposActividad();
    }

    private void cargarTiposActividad() {
        tipoActividadDAO.getAllTiposActividad(new TipoActividadDAO.OnTiposActividadLoadedListener() {
            @Override
            public void onTiposActividadLoaded(ArrayList<TipoActividad> tiposActividad) {
                runOnUiThread(() -> {
                    listaTipos.clear();
                    listaTipos.addAll(tiposActividad);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(TipoActividadActivity.this,
                        "Error al cargar los tipos de actividad", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 👁️ Ver detalles
    private void mostrarDetalles(TipoActividad tipo) {
        new AlertDialog.Builder(this)
                .setTitle("Detalles")
                .setMessage("Nombre: " + tipo.getNombre() +
                        "\n\nDescripción: " + tipo.getDescripcion() +
                        "\n\nCategoría: " + tipo.getCategoria())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // ➕ Agregar nuevo
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tipo_actividad, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spCategoria = dialogView.findViewById(R.id.spCategoria);

        ArrayAdapter<TipoActividad.Categoria> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TipoActividad.Categoria.values());
        spCategoria.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Nueva Actividad")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String descripcion = etDescripcion.getText().toString().trim();
                    TipoActividad.Categoria categoria = (TipoActividad.Categoria) spCategoria.getSelectedItem();

                    if (nombre.isEmpty() || descripcion.isEmpty()) {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TipoActividad nuevoTipo = new TipoActividad(nombre, descripcion, categoria);
                    tipoActividadDAO.saveTipoActividad(nuevoTipo, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(TipoActividadActivity.this, "Tipo de actividad registrado", Toast.LENGTH_SHORT).show();
                                cargarTiposActividad();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(TipoActividadActivity.this,
                                    "No fue posible registrar el tipo de actividad", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ✏️ Editar
    private void mostrarDialogoEditar(TipoActividad tipo, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tipo_actividad, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Spinner spCategoria = dialogView.findViewById(R.id.spCategoria);

        etNombre.setText(tipo.getNombre());
        etDescripcion.setText(tipo.getDescripcion());

        ArrayAdapter<TipoActividad.Categoria> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TipoActividad.Categoria.values());
        spCategoria.setAdapter(spinnerAdapter);
        spCategoria.setSelection(tipo.getCategoria().ordinal());

        new AlertDialog.Builder(this)
                .setTitle("Editar Actividad")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = etNombre.getText().toString().trim();
                    String nuevaDescripcion = etDescripcion.getText().toString().trim();
                    TipoActividad.Categoria nuevaCategoria = (TipoActividad.Categoria) spCategoria.getSelectedItem();

                    if (nuevoNombre.isEmpty() || nuevaDescripcion.isEmpty()) {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TipoActividad tipoActualizado = new TipoActividad(nuevoNombre, nuevaDescripcion, nuevaCategoria);
                    tipoActualizado.setId(tipo.getId());
                    tipoActividadDAO.saveTipoActividad(tipoActualizado, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(TipoActividadActivity.this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
                                cargarTiposActividad();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(TipoActividadActivity.this,
                                    "No fue posible actualizar el tipo de actividad", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar
    private void confirmarEliminar(int position) {
        TipoActividad tipoActividad = listaTipos.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este tipo de actividad?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    tipoActividadDAO.deleteTipoActividad(tipoActividad, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(TipoActividadActivity.this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarTiposActividad();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(TipoActividadActivity.this,
                                    "No fue posible eliminar el tipo de actividad", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
