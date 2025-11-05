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
import com.test.mysede.R;
import com.test.mysede.model.Lugar;

import java.util.ArrayList;
import java.util.List;

public class LugarActivity extends AppCompatActivity {

    private RecyclerView recyclerLugares;
    private LugarAdapter adapter;
    private List<Lugar> listaLugares;
    private FloatingActionButton btnNuevoLugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lugar);

        recyclerLugares = findViewById(R.id.recyclerLugares);
        btnNuevoLugar = findViewById(R.id.btnNuevoLugar);

        listaLugares = new ArrayList<>();
        listaLugares.add(new Lugar("Centro Comunitario", Lugar.Tipo.OFICINA_DEL_CENTRO, 25));
        listaLugares.add(new Lugar("Sede Vecinal Norte", Lugar.Tipo.LUGAR_DEL_TERRITORIO, 40));

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

                    Integer cupo = cupoStr.isEmpty() ? null : Integer.parseInt(cupoStr);
                    listaLugares.add(new Lugar(nombre, tipo, cupo));
                    adapter.notifyItemInserted(listaLugares.size() - 1);
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

                    Integer nuevoCupo = nuevoCupoStr.isEmpty() ? null : Integer.parseInt(nuevoCupoStr);
                    listaLugares.set(position, new Lugar(nuevoNombre, nuevoTipo, nuevoCupo));
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Lugar actualizado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar lugar
    private void confirmarEliminar(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este lugar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    listaLugares.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Lugar eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
