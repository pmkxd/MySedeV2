package com.test.mysede.actividades;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.test.mysede.DAO.ActividadDAO;
import com.test.mysede.DAO.ArchivoAdjuntoDAO;
import com.test.mysede.DAO.CitaDAO;
import com.test.mysede.DAO.FirestoreOperationCallback;

import com.test.mysede.R;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.ArchivoAdjunto;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.TipoActividad;
import com.test.mysede.ui.SystemBarsHelper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class VerActividadActivity extends AppCompatActivity {

    private TextView tvNombre, tvTipo, tvPeriodicidad, tvFechas, tvCupo;
    private TextView tvProyecto, tvOferentes, tvSocio, tvDiasAviso;
    private Button btnEditar, btnEliminar;
    private LinearLayout layoutArchivosAdjuntos;
    private Actividad actividad;
    private String actividadId;
    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final ArchivoAdjuntoDAO archivoAdjuntoDAO = new ArchivoAdjuntoDAO();
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
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

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
        layoutArchivosAdjuntos = findViewById(R.id.layoutArchivosAdjuntos);
        actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId == null) {
            Toast.makeText(this, "No se encontró la actividad seleccionada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ============================================
        // CONFIGURAR BOTONES CON VALIDACIÓN DE PERMISOS
        // ============================================
        configurarBotones();

        cargarActividad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (actividadId != null) {
            cargarActividad();
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
                intent.putExtra("modo", "editar");
                intent.putExtra("actividadId", actividadId);
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

        mostrarArchivosAdjuntos();
    }

    private void mostrarArchivosAdjuntos() {
        if (layoutArchivosAdjuntos == null) {
            return;
        }
        layoutArchivosAdjuntos.removeAllViews();

        if (actividad == null) {
            return;
        }

        List<ArchivoAdjunto> adjuntos = actividad.getArchivosAdjuntos();
        if (adjuntos == null || adjuntos.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Sin archivos adjuntos");
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onSurfaceVariant));
            layoutArchivosAdjuntos.addView(emptyView);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        boolean puedeRenombrar = PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD);
        boolean puedeEliminar = PermissionManager.tienePermiso(Permiso.ELIMINAR_ACTIVIDAD);

        for (ArchivoAdjunto archivo : adjuntos) {
            View itemView = inflater.inflate(R.layout.item_archivo_adjunto_detalle, layoutArchivosAdjuntos, false);

            TextView nombre = itemView.findViewById(R.id.tvNombreArchivo);
            TextView tipo = itemView.findViewById(R.id.tvTipoArchivo);
            TextView tamaño = itemView.findViewById(R.id.tvTamanoArchivo);
            MaterialButton btnVer = itemView.findViewById(R.id.btnVerArchivo);
            MaterialButton btnDescargar = itemView.findViewById(R.id.btnDescargarArchivo);
            MaterialButton btnRenombrar = itemView.findViewById(R.id.btnRenombrarArchivo);
            MaterialButton btnEliminar = itemView.findViewById(R.id.btnEliminarArchivo);

            nombre.setText("📄 " + archivo.getNombre());
            tipo.setText("Tipo: " + (archivo.getTipo() != null ? archivo.getTipo() : "Desconocido"));
            tamaño.setText("Tamaño: " + formatearTamano(archivo.getTamaño()));

            btnVer.setOnClickListener(v -> abrirArchivo(archivo));
            btnDescargar.setOnClickListener(v -> descargarArchivo(archivo));

            if (puedeRenombrar) {
                btnRenombrar.setOnClickListener(v -> mostrarDialogoRenombrarArchivo(archivo));
            } else {
                btnRenombrar.setVisibility(View.GONE);
            }

            if (puedeEliminar) {
                btnEliminar.setOnClickListener(v -> mostrarDialogoEliminarArchivo(archivo));
            } else {
                btnEliminar.setVisibility(View.GONE);
            }

            layoutArchivosAdjuntos.addView(itemView);
        }
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format(Locale.getDefault(), "%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format(Locale.getDefault(), "%.1f GB", gb);
    }

    private void abrirArchivo(ArchivoAdjunto archivo) {
        if (archivo == null || TextUtils.isEmpty(archivo.getUrl())) {
            Toast.makeText(this, "El archivo no tiene una URL disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(archivo.getUrl()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No se encontró una aplicación para abrir el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void descargarArchivo(ArchivoAdjunto archivo) {
        if (archivo == null || TextUtils.isEmpty(archivo.getUrl())) {
            Toast.makeText(this, "No se encontró la URL del archivo", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Toast.makeText(this, "No se pudo acceder al gestor de descargas", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(archivo.getUrl()));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle(archivo.getNombre());
            request.setDescription("Descargando archivo adjunto");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, archivo.getNombre());
            downloadManager.enqueue(request);
            Toast.makeText(this, "Descarga iniciada", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException | SecurityException e) {
            Toast.makeText(this, "No fue posible iniciar la descarga", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoRenombrarArchivo(ArchivoAdjunto archivo) {
        if (!PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD)) {
            Toast.makeText(this, "No tienes permiso para renombrar archivos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (archivo == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Renombrar archivo");

        final EditText input = new EditText(this);
        String extension = obtenerExtension(archivo.getNombre());
        String nombreBase = archivo.getNombre();
        if (!TextUtils.isEmpty(extension) && nombreBase.endsWith(extension)) {
            nombreBase = nombreBase.substring(0, nombreBase.length() - extension.length());
        }
        input.setText(nombreBase);
        input.setSelection(nombreBase.length());
        builder.setView(input);
        builder.setNegativeButton("Cancelar", null);
        builder.setPositiveButton("Guardar", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String nuevoNombreBase = input.getText().toString().trim();
                if (nuevoNombreBase.isEmpty()) {
                    input.setError("El nombre no puede estar vacío");
                    return;
                }
                String extensionActual = obtenerExtension(archivo.getNombre());
                String nombreFinal = nuevoNombreBase;
                if (!TextUtils.isEmpty(extensionActual) && !nuevoNombreBase.endsWith(extensionActual)) {
                    nombreFinal = nuevoNombreBase + extensionActual;
                }
                if (nombreFinal.equals(archivo.getNombre())) {
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                ejecutarRenombrarArchivo(archivo, nombreFinal);
            });
        });
        dialog.show();
    }

    private void ejecutarRenombrarArchivo(ArchivoAdjunto archivo, String nombreFinal) {
        if (archivo == null) {
            return;
        }
        if (TextUtils.isEmpty(archivo.getId())) {
            Toast.makeText(this, "El archivo no se puede renombrar porque no tiene un identificador", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombreAnterior = archivo.getNombre();
        ArchivoAdjunto actualizado = clonarArchivo(archivo);
        actualizado.setNombre(nombreFinal);

        archivoAdjuntoDAO.updateArchivoAdjunto(actualizado, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                archivo.setNombre(nombreFinal);
                List<ArchivoAdjunto> adjuntosActuales = new ArrayList<>(actividad.getArchivosAdjuntos());
                actividad.setArchivosAdjuntos(adjuntosActuales);
                actividadDAO.updateActividad(actividad, new FirestoreOperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(VerActividadActivity.this, "Archivo renombrado correctamente", Toast.LENGTH_SHORT).show();
                        mostrarArchivosAdjuntos();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        archivo.setNombre(nombreAnterior);
                        Toast.makeText(VerActividadActivity.this, "No fue posible actualizar la actividad", Toast.LENGTH_SHORT).show();
                        cargarActividad();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerActividadActivity.this, "Error al renombrar el archivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoEliminarArchivo(ArchivoAdjunto archivo) {
        if (!PermissionManager.tienePermiso(Permiso.ELIMINAR_ACTIVIDAD)) {
            Toast.makeText(this, "No tienes permiso para eliminar archivos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (archivo == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar archivo")
                .setMessage("¿Desea eliminar el archivo \"" + archivo.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarArchivoAdjunto(archivo))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarArchivoAdjunto(ArchivoAdjunto archivo) {
        if (archivo == null) {
            return;
        }
        if (TextUtils.isEmpty(archivo.getId())) {
            removerArchivoDeActividad(archivo);
            return;
        }
        archivoAdjuntoDAO.deleteArchivoAdjunto(archivo.getId(), new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                removerArchivoDeActividad(archivo);
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerActividadActivity.this, "No fue posible eliminar el archivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removerArchivoDeActividad(ArchivoAdjunto archivo) {
        if (actividad == null) {
            return;
        }
        List<ArchivoAdjunto> adjuntosActuales = new ArrayList<>(actividad.getArchivosAdjuntos());
        ArchivoAdjunto encontrado = null;
        for (ArchivoAdjunto item : adjuntosActuales) {
            if (coincideArchivo(item, archivo)) {
                encontrado = item;
                break;
            }
        }
        if (encontrado == null) {
            Toast.makeText(this, "El archivo ya no está disponible", Toast.LENGTH_SHORT).show();
            mostrarArchivosAdjuntos();
            return;
        }
        adjuntosActuales.remove(encontrado);
        actividad.setArchivosAdjuntos(adjuntosActuales);
        actividadDAO.updateActividad(actividad, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(VerActividadActivity.this, "Archivo eliminado correctamente", Toast.LENGTH_SHORT).show();
                mostrarArchivosAdjuntos();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerActividadActivity.this, "No fue posible actualizar la actividad", Toast.LENGTH_SHORT).show();
                cargarActividad();
            }
        });
    }

    private boolean coincideArchivo(ArchivoAdjunto a, ArchivoAdjunto b) {
        if (a == null || b == null) {
            return false;
        }
        if (!TextUtils.isEmpty(a.getId()) && !TextUtils.isEmpty(b.getId())) {
            return TextUtils.equals(a.getId(), b.getId());
        }
        return TextUtils.equals(a.getUrl(), b.getUrl());
    }

    private ArchivoAdjunto clonarArchivo(ArchivoAdjunto original) {
        ArchivoAdjunto copia = new ArchivoAdjunto(
                original.getNombre(),
                original.getTipo(),
                original.getTamaño(),
                original.getUri(),
                original.getUrl(),
                original.getResourceType(),
                original.getUploadPreset(),
                original.getPublicId()
        );
        copia.setId(original.getId());
        return copia;
    }

    private String obtenerExtension(String nombre) {
        if (TextUtils.isEmpty(nombre)) {
            return "";
        }
        int index = nombre.lastIndexOf('.');
        if (index == -1 || index == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(index);
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
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarActividad())
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

    private void cargarActividad() {
        actividadDAO.getActividadById(actividadId, new ActividadDAO.OnActividadLoadedListener() {
            @Override
            public void onActividadLoaded(Actividad actividadCargada) {
                if (actividadCargada == null) {
                    Toast.makeText(VerActividadActivity.this, "No se encontró la actividad", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                actividad = actividadCargada;
                citaDAO.getCitasPorActividad(actividad, new CitaDAO.OnCitasLoadedListener() {
                    @Override
                    public void onCitasLoaded(ArrayList<com.test.mysede.model.Cita> citas) {
                        mostrarDatos();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(VerActividadActivity.this, "No fue posible cargar las citas", Toast.LENGTH_SHORT).show();
                        mostrarDatos();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(VerActividadActivity.this, "Error al cargar la actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarActividad() {
        if (actividad == null) {
            return;
        }
        citaDAO.deleteCitasPorActividad(actividad, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                actividadDAO.deleteActividad(actividad, new FirestoreOperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(VerActividadActivity.this, "Actividad eliminada correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Toast.makeText(VerActividadActivity.this, "Error al eliminar la actividad", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerActividadActivity.this, "No fue posible eliminar las citas asociadas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}