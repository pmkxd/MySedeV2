package com.test.mysede.oferente;

import android.app.AlertDialog;
import android.content.Intent; // Import para abrir nuevas actividades
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu; // Import para inflar el menú
import android.view.MenuItem; //  Import para manejar clics del menú
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull; // Para usar @NonNull en onOptionsItemSelected
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.OferenteActividadDAO;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager; // Manejo de sesión
import com.test.mysede.auth.SessionManager; //  Sesión local

import com.test.mysede.auth.Permiso;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.Usuario;
import com.test.mysede.perfil.PerfilActivity; //  Para abrir el perfil del usuario
import com.test.mysede.login.ActivityLogin; //  Para redirigir al login si la sesión es inválida
import com.test.mysede.ui.SystemBarsHelper;
import com.test.mysede.perfil.ProfileImageLoader;
import java.util.ArrayList;
import java.util.List;

public class OferenteActivity extends AppCompatActivity {

    private RecyclerView recyclerOferentes;
    private OferenteAdapter adapter;
    private List<OferenteActividad> listaOferentes;
    private FloatingActionButton btnNuevoOferente;
    private OferenteActividadDAO oferenteActividadDAO;

    //  Manejadores de sesión y usuario
    private SessionManager sessionManager;
    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferente);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        //  Configurar sesión y usuario actual
        sessionManager = new SessionManager(this);
        usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            usuarioActual = sessionManager.obtenerUsuarioSesion();
            if (usuarioActual != null) {
                PermissionManager.setUsuarioActual(usuarioActual);
            }
        }
        if (usuarioActual == null) {
            Toast.makeText(this, "Sesión inválida, por favor ingresa nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        // Configurar Toolbar con el nombre del usuario y menú de opciones
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setSubtitle(usuarioActual.getNombre());
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        

        //  Inicializar componentes de la vista

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

        btnNuevoOferente.setOnClickListener(v -> {
            if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_OFERENTES)) {
                Toast.makeText(this, "No tienes permiso para crear oferentes", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarDialogoAgregar();
        });

        cargarOferentes();
    }

    //  Inflar el menú con la opción de perfil
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_options, menu);
        ProfileImageLoader.loadIntoMenuItem(this, menu.findItem(R.id.menu_perfil),
                usuarioActual != null ? usuarioActual.getProfileImageUrl() : null);
        return true;
    }

    //  Manejar clics del menú (solo Perfil)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_perfil) {
            //  Ir al perfil del usuario
            startActivity(new Intent(this, PerfilActivity.class));
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    //  Cargar oferentes desde Firebase
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
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_OFERENTES)) {
                        Toast.makeText(this, "No tienes permiso para crear oferentes", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nombre = etNombre.getText().toString().trim();
                    String docente = etDocente.getText().toString().trim();
                    OferenteActividad.Institucion institucion = (OferenteActividad.Institucion) spInstitucion.getSelectedItem();

                    if (nombre.isEmpty() || docente.isEmpty()) {
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nombre.length() < 3) {
                        Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (docente.length() < 3) {
                        Toast.makeText(this, "El nombre del docente debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validar duplicados
                    if (existeOferenteConNombre(nombre)) {
                        Toast.makeText(this, "Ya existe un oferente con este nombre", Toast.LENGTH_SHORT).show();
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
                    if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_OFERENTES)) {
                        Toast.makeText(this, "No tienes permiso para editar oferentes", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nuevoNombre = etNombre.getText().toString().trim();
                    String nuevoDocente = etDocente.getText().toString().trim();
                    OferenteActividad.Institucion nuevaInstitucion = (OferenteActividad.Institucion) spInstitucion.getSelectedItem();

                    if (nuevoNombre.isEmpty() || nuevoDocente.isEmpty()) {
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nuevoNombre.length() < 3) {
                        Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nuevoDocente.length() < 3) {
                        Toast.makeText(this, "El nombre del docente debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validar duplicados (excluir el oferente actual)
                    if (existeOferenteConNombreExcepto(nuevoNombre, oferente.getId())) {
                        Toast.makeText(this, "Ya existe un oferente con este nombre", Toast.LENGTH_SHORT).show();
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
        if (!PermissionManager.tienePermiso(Permiso.GESTIONAR_OFERENTES)) {
            Toast.makeText(this, "No tienes permiso para eliminar oferentes", Toast.LENGTH_SHORT).show();
            return;
        }
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

    /**
     * Verifica si ya existe un oferente con el nombre dado
     */
    private boolean existeOferenteConNombre(String nombre) {
        for (OferenteActividad o : listaOferentes) {
            if (o.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si ya existe un oferente con el nombre dado, excluyendo un ID específico
     */
    private boolean existeOferenteConNombreExcepto(String nombre, String idExcluir) {
        for (OferenteActividad o : listaOferentes) {
            if (o.getNombre().equalsIgnoreCase(nombre) && !o.getId().equals(idExcluir)) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        Usuario actualizado = sessionManager.obtenerUsuarioSesion();
        if (actualizado != null) {
            usuarioActual = actualizado;
            PermissionManager.setUsuarioActual(actualizado);
        }
        invalidateOptionsMenu();
    }
}
