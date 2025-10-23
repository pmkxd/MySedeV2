package com.test.mysede.socio;

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
import com.test.mysede.model.SocioComunitario;

import java.util.ArrayList;
import java.util.List;

public class SocioComunitarioActivity extends AppCompatActivity {

    private RecyclerView recyclerSocios;
    private SocioComunitarioAdapter adapter;
    private List<SocioComunitario> listaSocios;
    private FloatingActionButton btnNuevoSocio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socio_comunitario);

        recyclerSocios = findViewById(R.id.recyclerSocios);
        btnNuevoSocio = findViewById(R.id.btnNuevoSocio);

        listaSocios = new ArrayList<>();
        listaSocios.add(new SocioComunitario("Juan Pérez"));
        listaSocios.add(new SocioComunitario("María González"));

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

        btnNuevoSocio.setOnClickListener(v -> mostrarDialogoAgregar());
    }

    // 👁️ Ver detalles
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
                    String nombre = etNombre.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaSocios.add(new SocioComunitario(nombre));
                    adapter.notifyItemInserted(listaSocios.size() - 1);
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
                    String nuevoNombre = etNombre.getText().toString().trim();

                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaSocios.set(position, new SocioComunitario(nuevoNombre));
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Socio actualizado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🗑️ Eliminar
    private void confirmarEliminar(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este socio?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    listaSocios.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Socio eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
