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
import com.test.mysede.R;
import com.test.mysede.model.TipoActividad;
import com.test.mysede.tipoactividad.TipoActividadAdapter;

import java.util.ArrayList;
import java.util.List;

public class TipoActividadActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TipoActividadAdapter adapter;
    private List<TipoActividad> listaTipos;
    private FloatingActionButton btnNuevaActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_actividad);

        recyclerView = findViewById(R.id.recyclerTipos);
        btnNuevaActividad = findViewById(R.id.btnNuevaActividad);

        listaTipos = new ArrayList<>();
        listaTipos.add(new TipoActividad("Taller de Cocina", "Aprende recetas saludables", TipoActividad.Categoria.TALLER));
        listaTipos.add(new TipoActividad("Charla Motivacional", "Encuentro para fomentar el bienestar", TipoActividad.Categoria.CHARLA));
        listaTipos.add(new TipoActividad("Operativo de Salud", "Atención médica gratuita", TipoActividad.Categoria.OPERATIVO));

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

                    listaTipos.add(new TipoActividad(nombre, descripcion, categoria));
                    adapter.notifyItemInserted(listaTipos.size() - 1);
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

                    listaTipos.set(position, new TipoActividad(nuevoNombre, nuevaDescripcion, nuevaCategoria));
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar
    private void confirmarEliminar(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este tipo de actividad?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    listaTipos.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
