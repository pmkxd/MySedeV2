package com.test.mysede.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.test.mysede.R;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.TipoActividad;

// ============================================
// IMPORTS DEL SISTEMA DE PERMISOS
// ============================================
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;

import java.time.format.DateTimeFormatter;

public class VerActividadActivity extends AppCompatActivity {

    private TextView tvNombre, tvTipo, tvPeriodicidad, tvFechas, tvCupo;
    private TextView tvProyecto, tvOferentes, tvSocio, tvDiasAviso;
    private Button btnEditar, btnEliminar;
    private int posicion;
    private Actividad actividad;

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

        setContentView(R.layout.activity_ver_actividad);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de Actividad");
        }

        // Inicializar vistas
        tvNombre = findViewById(R.id.tvNombre);
        tvTipo = findViewById(R.id.tvTipo);
        tvPeriodicidad = findViewById(R.id.tvPeriodicidad);
        tvFechas = findViewById(R.id.tvFechas);
        tvCupo = findViewById(R.id.tvCupo);
        tvProyecto = findViewById(R.id.tvProyecto);
        tvOferentes = findViewById(R.id.tvOferentes);
        tvSocio = findViewById(R.id.tvSocio);
        tvDiasAviso = findViewById(R.id.tvDiasAviso);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);

        // Obtener datos del intent
        posicion = getIntent().getIntExtra("posicion", -1);

        if (posicion != -1) {
            actividad = ActividadHelper.obtenerActividadPorIndice(posicion);
            mostrarDatos();
        }

        // ============================================
        // CONFIGURAR BOTONES CON VALIDACIÓN DE PERMISOS
        // ============================================
        configurarBotones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (posicion != -1) {
            actividad = ActividadHelper.obtenerActividadPorIndice(posicion);
            mostrarDatos();
        }
    }

    private void configurarBotones() {
        // ============================================
        // BOTÓN EDITAR - Solo si tiene permiso
        // ============================================
        if (PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD)) {
            btnEditar.setVisibility(View.VISIBLE);
            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(VerActividadActivity.this, CrearActividadActivity.class);
                intent.putExtra("posicion", posicion);
                intent.putExtra("modo", "editar");
                startActivity(intent);
            });
        } else {
            btnEditar.setVisibility(View.GONE);
        }

        // ============================================
        // BOTÓN ELIMINAR - Solo si tiene permiso
        // ============================================
        if (PermissionManager.tienePermiso(Permiso.ELIMINAR_ACTIVIDAD)) {
            btnEliminar.setVisibility(View.VISIBLE);
            btnEliminar.setOnClickListener(v -> mostrarDialogoEliminar());
        } else {
            btnEliminar.setVisibility(View.GONE);
        }
    }

    private void mostrarDatos() {
        if (actividad == null) return;

        // Nombre
        tvNombre.setText(actividad.getNombre());

        // Tipo de actividad
        if (!actividad.getTiposActividad().isEmpty()) {
            StringBuilder tipos = new StringBuilder();
            for (TipoActividad tipo : actividad.getTiposActividad()) {
                tipos.append(tipo.getNombre()).append(" (").append(tipo.getCategoria()).append(")\n");
            }
            tvTipo.setText(tipos.toString().trim());
        } else {
            tvTipo.setText("Sin tipo definido");
        }

        // Periodicidad
        tvPeriodicidad.setText(actividad.getPeriodicidad().getTipo().toString());

        // Fechas según periodicidad
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (actividad.getPeriodicidad().getFechaInicio().isPresent()) {
            String fechas = "";
            if (actividad.getPeriodicidad().getTipo().toString().equals("PUNTUAL")) {
                fechas = "Fecha: " + actividad.getPeriodicidad().getFechaInicio().get().format(formatter);
            } else {
                fechas = "Desde: " + actividad.getPeriodicidad().getFechaInicio().get().format(formatter);
                if (actividad.getPeriodicidad().getFechaFin().isPresent()) {
                    fechas += "\nHasta: " + actividad.getPeriodicidad().getFechaFin().get().format(formatter);
                }
            }
            tvFechas.setText(fechas);
        } else {
            tvFechas.setText("Sin fechas definidas");
        }

        // Cupo
        if (actividad.getCupo() != null) {
            tvCupo.setText("Cupo: " + actividad.getCupo() + " personas");
        } else {
            tvCupo.setText("Sin cupo definido");
        }

        // Proyecto
        if (actividad.getProyecto() != null) {
            tvProyecto.setText(actividad.getProyecto().getNombre());
        } else {
            tvProyecto.setText("Sin proyecto asignado");
        }

        // Oferentes
        if (!actividad.getOferentes().isEmpty()) {
            StringBuilder oferentes = new StringBuilder();
            for (OferenteActividad oferente : actividad.getOferentes()) {
                oferentes.append("• ").append(oferente.getNombre())
                        .append(" - ").append(oferente.getDocenteResponsable())
                        .append(" (").append(oferente.getInstitucion()).append(")\n");
            }
            tvOferentes.setText(oferentes.toString().trim());
        } else {
            tvOferentes.setText("Sin oferentes asignados");
        }

        // Socio comunitario
        if (actividad.getSocioComunitario() != null) {
            tvSocio.setText(actividad.getSocioComunitario().getNombre());
        } else {
            tvSocio.setText("Sin socio comunitario");
        }

        // Días de aviso previo
        tvDiasAviso.setText(actividad.getDiasAvisoPrevio() + " días antes");
    }

    private void mostrarDialogoEliminar() {
        // ============================================
        // VALIDACIÓN ADICIONAL ANTES DE ELIMINAR
        // ============================================
        if (!PermissionManager.tienePermiso(Permiso.ELIMINAR_ACTIVIDAD)) {
            Toast.makeText(this, "No tienes permiso para eliminar actividades", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Actividad")
                .setMessage("¿Está seguro que desea eliminar esta actividad?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ActividadHelper.eliminarActividad(posicion);
                    Toast.makeText(this, "Actividad eliminada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}