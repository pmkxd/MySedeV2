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

import com.google.android.material.appbar.MaterialToolbar;
import com.test.mysede.ui.SystemBarsHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.OferenteActividadDAO;
import com.test.mysede.R;
import com.test.mysede.model.OferenteActividad;

import java.util.ArrayList;
import java.util.List;

public class OferenteActivity extends AppCompatActivity {

    private RecyclerView recyclerOferentes;
    private OferenteAdapter adapter;
    private List<OferenteActividad> listaOferentes;
    private FloatingActionButton btnNuevoOferente;

    private OferenteActividadDAO oferenteActividadDAO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferente);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerOferentes = findViewById(R.id.recyclerOferentes);
        btnNuevoOferente = findViewById(R.id.btnNuevoOferente);
        oferenteActividadDAO = new OferenteActividadDAO();
        listaOferentes = new ArrayList<>();


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

        cargarOferentes();
    }

    private void cargarOferentes() {
        oferenteActividadDAO.getAllOferentes(new OferenteActividadDAO.OnOferentesLoadedListener() {
            @Override
            public void onOferentesLoaded(ArrayList<OferenteActividad> oferentes) {
                runOnUiThread(() -> {
                    listaOferentes.clear();
                    listaOferentes.addAll(oferentes);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(OferenteActivity.this,
                        "Error al cargar los oferentes", Toast.LENGTH_SHORT).show());
            }
        });
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
                    OferenteActividad nuevoOferente = new OferenteActividad(nombre, docente, institucion);
                    oferenteActividadDAO.saveOferente(nuevoOferente, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(OferenteActivity.this, "Oferente registrado", Toast.LENGTH_SHORT).show();
                                cargarOferentes();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(OferenteActivity.this,
                                    "No fue posible registrar el oferente", Toast.LENGTH_SHORT).show());
                        }
                    });
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
                    OferenteActividad oferenteActualizado = new OferenteActividad(nuevoNombre, nuevoDocente, nuevaInstitucion);
                    oferenteActualizado.setId(oferente.getId());
                    oferenteActividadDAO.saveOferente(oferenteActualizado, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(OferenteActivity.this, "Oferente actualizado", Toast.LENGTH_SHORT).show();
                                cargarOferentes();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(OferenteActivity.this,
                                    "No fue posible actualizar el oferente", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminar(int position) {
        OferenteActividad oferente = listaOferentes.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Seguro que deseas eliminar este oferente?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    oferenteActividadDAO.deleteOferente(oferente, new FirestoreOperationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(OferenteActivity.this, "Oferente eliminado", Toast.LENGTH_SHORT).show();
                                cargarOferentes();
                            });
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(OferenteActivity.this,
                                    "No fue posible eliminar el oferente", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
