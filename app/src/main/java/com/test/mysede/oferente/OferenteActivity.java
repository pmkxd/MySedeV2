package com.test.mysede.oferente;

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
import com.test.mysede.model.OferenteActividad;

import java.util.ArrayList;
import java.util.List;

public class OferenteActivity extends AppCompatActivity {

    private RecyclerView recyclerOferentes;
    private OferenteAdapter adapter;
    private List<OferenteActividad> listaOferentes;
    private FloatingActionButton btnNuevoOferente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferente);

        recyclerOferentes = findViewById(R.id.recyclerOferentes);
        btnNuevoOferente = findViewById(R.id.btnNuevoOferente);

        listaOferentes = new ArrayList<>();
        listaOferentes.add(new OferenteActividad("Oferente 1", "Juan Pérez", OferenteActividad.Institucion.IP));
        listaOferentes.add(new OferenteActividad("Oferente 2", "María Gómez", OferenteActividad.Institucion.UNIVERSIDAD));

        adapter = new OferenteAdapter(this, listaOferentes, new OferenteAdapter.OnItemClickListener() {
            @Override
            public void onVer(OferenteActividad oferente) {
                mostrarDetalles(oferente);
            }

            @Override
            public void onEditar(OferenteActividad oferente, int position) {
                mostrarDialogoEditar(oferente, position);
            }

            @Override
            public void onEliminar(int position) {
                confirmarEliminar(position);
            }
        });

        recyclerOferentes.setLayoutManager(new LinearLayoutManager(this));
        recyclerOferentes.setAdapter(adapter);

        btnNuevoOferente.setOnClickListener(v -> mostrarDialogoAgregar());
    }

    private void mostrarDetalles(OferenteActividad oferente) {
        new AlertDialog.Builder(this)
                .setTitle("Detalles del Oferente")
                .setMessage("Nombre: " + oferente.getNombre() +
                        "\nDocente Responsable: " + oferente.getDocenteResponsable() +
                        "\nInstitución: " + oferente.getInstitucion())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_oferente, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreOferente);
        EditText etDocente = dialogView.findViewById(R.id.etDocenteResponsable);
        Spinner spInstitucion = dialogView.findViewById(R.id.spInstitucion);

        ArrayAdapter<OferenteActividad.Institucion> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, OferenteActividad.Institucion.values());
        spInstitucion.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Oferente")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String docente = etDocente.getText().toString().trim();
                    OferenteActividad.Institucion institucion = (OferenteActividad.Institucion) spInstitucion.getSelectedItem();

                    if (nombre.isEmpty() || docente.isEmpty()) {
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaOferentes.add(new OferenteActividad(nombre, docente, institucion));
                    adapter.notifyItemInserted(listaOferentes.size() - 1);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditar(OferenteActividad oferente, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_oferente, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreOferente);
        EditText etDocente = dialogView.findViewById(R.id.etDocenteResponsable);
        Spinner spInstitucion = dialogView.findViewById(R.id.spInstitucion);

        etNombre.setText(oferente.getNombre());
        etDocente.setText(oferente.getDocenteResponsable());

        ArrayAdapter<OferenteActividad.Institucion> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, OferenteActividad.Institucion.values());
        spInstitucion.setAdapter(spinnerAdapter);
        spInstitucion.setSelection(oferente.getInstitucion().ordinal());

        new AlertDialog.Builder(this)
                .setTitle("Editar Oferente")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = etNombre.getText().toString().trim();
                    String nuevoDocente = etDocente.getText().toString().trim();
                    OferenteActividad.Institucion nuevaInstitucion = (OferenteActividad.Institucion) spInstitucion.getSelectedItem();

                    if (nuevoNombre.isEmpty() || nuevoDocente.isEmpty()) {
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaOferentes.set(position, new OferenteActividad(nuevoNombre, nuevoDocente, nuevaInstitucion));
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Oferente actualizado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminar(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este oferente?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    listaOferentes.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Oferente eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
