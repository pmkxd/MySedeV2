package com.test.mysede.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.ActividadDAO;
import com.test.mysede.R;
import com.test.mysede.model.Actividad;

// ============================================
// IMPORTS DEL SISTEMA DE PERMISOS
// ============================================
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import java.util.ArrayList;
import java.util.List;

public class ListarActividadesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActividadAdapter adapter;
    private FloatingActionButton fabCrear;
    private final List<Actividad> actividades = new ArrayList<>();
    private final ActividadDAO actividadDAO = new ActividadDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ============================================
        // VALIDAR PERMISO PARA VER ACTIVIDADES
        // ============================================
        if (!PermissionManager.tienePermiso(Permiso.VER_ACTIVIDADES)) {
            Toast.makeText(this, "No tienes permiso para ver actividades", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_listar_actividades);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Actividades");
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewActividades);
        fabCrear = findViewById(R.id.fabCrearActividad);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ActividadAdapter(this, actividades, actividad -> {
            Intent intent = new Intent(ListarActividadesActivity.this, VerActividadActivity.class);
            intent.putExtra("actividadId", actividad.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // ============================================
        // CONFIGURAR FAB CON VALIDACIÓN DE PERMISOS
        // ============================================
        if (PermissionManager.tienePermiso(Permiso.CREAR_ACTIVIDAD)) {
            fabCrear.setVisibility(View.VISIBLE);
            fabCrear.setOnClickListener(v -> {
                Intent intent = new Intent(ListarActividadesActivity.this, CrearActividadActivity.class);
                startActivity(intent);
            });
        } else {
            fabCrear.setVisibility(View.GONE);
        }
        cargarActividades();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarActividades();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarActividades() {
        actividadDAO.getAllActividades(new ActividadDAO.OnActividadesLoadedListener() {
            @Override
            public void onActividadesLoaded(ArrayList<Actividad> actividadesCargadas) {
                actividades.clear();
                actividades.addAll(actividadesCargadas);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ListarActividadesActivity.this, "Error al cargar actividades", Toast.LENGTH_SHORT).show();
            }
        });
    }
}