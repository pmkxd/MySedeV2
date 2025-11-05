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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.R;
import com.test.mysede.model.Proyecto;

import java.util.ArrayList;
import java.util.List;

public class ProyectoActivity extends AppCompatActivity {

    private RecyclerView recyclerProyectos;
    private ProyectoAdapter adapter;
    private List<Proyecto> listaProyectos;
    private FloatingActionButton btnNuevoProyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyecto);

        recyclerProyectos = findViewById(R.id.recyclerProyectos);
        btnNuevoProyecto = findViewById(R.id.btnNuevoProyecto);

        listaProyectos = new ArrayList<>();
        listaProyectos.add(new Proyecto("Proyecto Inicial 1"));
        listaProyectos.add(new Proyecto("Proyecto Inicial 2"));

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

        btnNuevoProyecto.setOnClickListener(v -> mostrarDialogoAgregar());
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
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listaProyectos.add(new Proyecto(nombre));
                    adapter.notifyItemInserted(listaProyectos.size() - 1);
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
                    String nuevoNombre = etNombre.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listaProyectos.set(position, new Proyecto(nuevoNombre));
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Proyecto actualizado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar
    private void confirmarEliminar(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este proyecto?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    listaProyectos.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
