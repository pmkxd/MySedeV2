package com.test.mysede.actividades;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.test.mysede.notificaciones.GestorNotificaciones;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.test.mysede.AdjuntarArchivosActivity;
import com.test.mysede.DAO.ActividadDAO;
import com.test.mysede.DAO.ArchivoAdjuntoDAO;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.R;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.ArchivoAdjunto;
import com.test.mysede.ui.SystemBarsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VerArchivosAdjuntosActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_AGREGAR_ARCHIVOS = 401;
    private static final String TAG = "VerArchivosAdjuntos";

    private LinearLayout layoutArchivosAdjuntos;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private MaterialButton btnAgregarArchivos;

    private Actividad actividad;
    private String actividadId;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final ArchivoAdjuntoDAO archivoAdjuntoDAO = new ArchivoAdjuntoDAO();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PermissionManager.tienePermiso(Permiso.VER_ARCHIVOS)) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_permission_view, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_ver_archivos_adjuntos);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.ver_archivos_adjuntos_title);
        }

        layoutArchivosAdjuntos = findViewById(R.id.layoutArchivosAdjuntos);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        btnAgregarArchivos = findViewById(R.id.btnAgregarArchivos);

        actividadId = getIntent().getStringExtra("actividadId");
        if (TextUtils.isEmpty(actividadId)) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnAgregarArchivos.setOnClickListener(v -> {
            if (!PermissionManager.tienePermiso(Permiso.ADJUNTAR_ARCHIVOS)) {
                Toast.makeText(this, R.string.ver_archivos_adjuntos_permission_add, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AdjuntarArchivosActivity.class);
            startActivityForResult(intent, REQUEST_CODE_AGREGAR_ARCHIVOS);
        });
        actualizarEstadoBotonAgregar(false);

        cargarActividad();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AGREGAR_ARCHIVOS && resultCode == RESULT_OK && data != null) {
            ArrayList<ArchivoAdjunto> nuevos = data.getParcelableArrayListExtra("archivosAdjuntos");
            procesarArchivosSeleccionados(nuevos);
        }
    }

    private void cargarActividad() {
        mostrarCargando(true);
        actividadDAO.getActividadById(actividadId, new ActividadDAO.OnActividadLoadedListener() {
            @Override
            public void onActividadLoaded(Actividad actividadCargada) {
                mostrarCargando(false);
                if (actividadCargada == null) {
                    Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                actividad = actividadCargada;
                mostrarArchivosAdjuntos();
            }

            @Override
            public void onError(Exception e) {
                mostrarCargando(false);
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarArchivosAdjuntos() {
        layoutArchivosAdjuntos.removeAllViews();
        if (actividad == null || actividad.getArchivosAdjuntos() == null || actividad.getArchivosAdjuntos().isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyState.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        boolean puedeEliminar = PermissionManager.tienePermiso(Permiso.ELIMINAR_ARCHIVOS);

        for (ArchivoAdjunto archivo : actividad.getArchivosAdjuntos()) {
            View itemView = inflater.inflate(R.layout.item_archivo_adjunto_detalle, layoutArchivosAdjuntos, false);

            TextView nombre = itemView.findViewById(R.id.tvNombreArchivo);
            TextView tipo = itemView.findViewById(R.id.tvTipoArchivo);
            TextView tamano = itemView.findViewById(R.id.tvTamanoArchivo);
            MaterialButton btnVer = itemView.findViewById(R.id.btnVerArchivo);
            MaterialButton btnDescargar = itemView.findViewById(R.id.btnDescargarArchivo);
            MaterialButton btnEliminar = itemView.findViewById(R.id.btnEliminarArchivo);

            nombre.setText("📄 " + archivo.getNombre());
            String tipoDescripcion = archivo.getTipo() != null ? archivo.getTipo() : getString(R.string.ver_archivos_adjuntos_tipo_desconocido);
            tipo.setText(getString(R.string.ver_archivos_adjuntos_tipo_formato, tipoDescripcion));
            tamano.setText("Tamaño: " + formatearTamano(archivo.getTamaño()));

            btnVer.setOnClickListener(v -> abrirArchivo(archivo));
            btnDescargar.setOnClickListener(v -> descargarArchivo(archivo));

            if (puedeEliminar) {
                btnEliminar.setOnClickListener(v -> mostrarDialogoEliminarArchivo(archivo));
            } else {
                btnEliminar.setVisibility(View.GONE);
            }

            layoutArchivosAdjuntos.addView(itemView);
        }
    }

    private void procesarArchivosSeleccionados(@Nullable ArrayList<ArchivoAdjunto> nuevos) {
        if (nuevos == null || nuevos.isEmpty()) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_no_selection, Toast.LENGTH_SHORT).show();
            return;
        }
        if (actividad == null) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarCargando(true);
        List<ArchivoAdjunto> adjuntosListos = new ArrayList<>();
        List<ArchivoAdjunto> adjuntosPorSubir = new ArrayList<>();
        for (ArchivoAdjunto archivo : nuevos) {
            if (archivo == null) {
                continue;
            }
            if (!TextUtils.isEmpty(archivo.getUrl())) {
                adjuntosListos.add(archivo);
            } else if (archivo.getUri() != null) {
                adjuntosPorSubir.add(archivo);
            }
        }
        if (adjuntosPorSubir.isEmpty()) {
            persistirArchivosAdjuntos(adjuntosListos);
            guardarNuevosAdjuntos(adjuntosListos);
            return;
        }
        List<Task<ArchivoAdjunto>> tareas = new ArrayList<>();
        for (ArchivoAdjunto archivo : adjuntosPorSubir) {
            tareas.add(archivoAdjuntoDAO.subirArchivoACloudinary(this, archivo));
        }
        Tasks.whenAllSuccess(tareas)
                .addOnSuccessListener(result -> {
                    List<ArchivoAdjunto> subidos = new ArrayList<>();
                    for (Object item : result) {
                        if (item instanceof ArchivoAdjunto) {
                            subidos.add((ArchivoAdjunto) item);
                        }
                    }
                    persistirArchivosAdjuntos(subidos);
                    adjuntosListos.addAll(subidos);
                    guardarNuevosAdjuntos(adjuntosListos);
                })
                .addOnFailureListener(e -> {
                    mostrarCargando(false);
                    Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error subiendo archivos", e);
                });
    }

    private void guardarNuevosAdjuntos(List<ArchivoAdjunto> nuevos) {
        if (actividad == null) {
            mostrarCargando(false);
            return;
        }
        List<ArchivoAdjunto> actuales = actividad.getArchivosAdjuntos() != null
                ? new ArrayList<>(actividad.getArchivosAdjuntos())
                : new ArrayList<>();
        actuales.addAll(nuevos);
        actividad.setArchivosAdjuntos(actuales);
        actividadDAO.updateActividad(actividad, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                mostrarCargando(false);
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_success_add, Toast.LENGTH_SHORT).show();

                // ========== ENVIAR NOTIFICACIONES DE NUEVO MATERIAL ==========
                enviarNotificacionesNuevoMaterial(nuevos);
                // =============================================================

                mostrarArchivosAdjuntos();
            }

            @Override
            public void onFailure(Exception exception) {
                mostrarCargando(false);
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void persistirArchivosAdjuntos(List<ArchivoAdjunto> adjuntos) {
        if (adjuntos == null || adjuntos.isEmpty()) {
            return;
        }
        for (ArchivoAdjunto archivo : adjuntos) {
            if (archivo == null || !TextUtils.isEmpty(archivo.getId())) {
                continue;
            }
            archivoAdjuntoDAO.guardarArchivo(archivo)
                    .addOnSuccessListener(ref -> archivo.setId(ref.getId()))
                    .addOnFailureListener(e -> Log.w(TAG, "No fue posible guardar el archivo adjunto", e));
        }
    }

    private void mostrarDialogoEliminarArchivo(ArchivoAdjunto archivo) {
        if (!PermissionManager.tienePermiso(Permiso.ELIMINAR_ARCHIVOS)) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_permission_add, Toast.LENGTH_SHORT).show();
            return;
        }
        if (archivo == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar)
                .setMessage(getString(R.string.ver_archivos_adjuntos_delete_message, archivo.getNombre()))
                .setPositiveButton(R.string.eliminar, (dialog, which) -> eliminarArchivoAdjunto(archivo))
                .setNegativeButton(android.R.string.cancel, null)
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
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removerArchivoDeActividad(ArchivoAdjunto archivo) {
        if (actividad == null || actividad.getArchivosAdjuntos() == null) {
            return;
        }
        List<ArchivoAdjunto> actuales = new ArrayList<>(actividad.getArchivosAdjuntos());
        ArchivoAdjunto encontrado = null;
        for (ArchivoAdjunto item : actuales) {
            if (coincideArchivo(item, archivo)) {
                encontrado = item;
                break;
            }
        }
        if (encontrado == null) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            mostrarArchivosAdjuntos();
            return;
        }

        // Guardar nombre del archivo antes de eliminarlo (para la notificación)
        final String nombreArchivoEliminado = encontrado.getNombre();

        actuales.remove(encontrado);
        actividad.setArchivosAdjuntos(actuales);
        actividadDAO.updateActividad(actividad, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_delete_success, Toast.LENGTH_SHORT).show();

                // ========== NOTIFICAR ELIMINACIÓN DE ARCHIVO ==========
                notificarArchivoEliminado(nombreArchivoEliminado);
                // ======================================================

                mostrarArchivosAdjuntos();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(archivo.getUrl()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_open_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void descargarArchivo(ArchivoAdjunto archivo) {
        if (archivo == null || TextUtils.isEmpty(archivo.getUrl())) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(archivo.getUrl()));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle(archivo.getNombre());
            request.setDescription(getString(R.string.ver_archivos_adjuntos_title));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, archivo.getNombre());
            downloadManager.enqueue(request);
            Toast.makeText(this, R.string.ver_archivos_adjuntos_download_start, Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException | SecurityException e) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarCargando(boolean mostrando) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrando ? View.VISIBLE : View.GONE);
        }
        actualizarEstadoBotonAgregar(mostrando);
    }

    private void actualizarEstadoBotonAgregar(boolean bloqueadoPorCarga) {
        if (btnAgregarArchivos == null) {
            return;
        }
        boolean puedeAdjuntar = PermissionManager.tienePermiso(Permiso.ADJUNTAR_ARCHIVOS);
        boolean habilitado = puedeAdjuntar && !bloqueadoPorCarga;
        btnAgregarArchivos.setEnabled(habilitado);
        btnAgregarArchivos.setAlpha(habilitado ? 1f : 0.5f);
    }

    /**
     * Envía notificaciones cuando se agregan archivos nuevos a una actividad
     */
    private void enviarNotificacionesNuevoMaterial(List<ArchivoAdjunto> archivosNuevos) {
        if (actividad == null || archivosNuevos == null || archivosNuevos.isEmpty()) {
            return;
        }

        GestorNotificaciones gestor = new GestorNotificaciones(this);

        // Si se subió un solo archivo, notificar con su nombre
        if (archivosNuevos.size() == 1) {
            ArchivoAdjunto archivo = archivosNuevos.get(0);
            gestor.notificarNuevoMaterial(
                    actividad.getId(),
                    actividad.getNombre(),
                    archivo.getNombre()
            );
            Log.d(TAG, "✓ Notificación enviada: Nuevo archivo - " + archivo.getNombre());
        }
        // Si se subieron múltiples archivos, notificar la cantidad
        else {
            String mensajeArchivos = archivosNuevos.size() + " archivos nuevos";
            gestor.notificarNuevoMaterial(
                    actividad.getId(),
                    actividad.getNombre(),
                    mensajeArchivos
            );
            Log.d(TAG, "✓ Notificación enviada: " + archivosNuevos.size() + " archivos nuevos");
        }
    }

    /**
     * Notifica cuando se elimina un archivo de una actividad
     */
    private void notificarArchivoEliminado(String nombreArchivo) {
        if (actividad == null || nombreArchivo == null) {
            return;
        }

        GestorNotificaciones gestor = new GestorNotificaciones(this);

        String titulo = "Archivo eliminado 🗑️";
        String mensaje = "Se eliminó " + nombreArchivo + " de " + actividad.getNombre();

        // Usar el método de cambio de actividad para notificar
        gestor.notificarCambioActividad(
                actividad.getId(),
                actividad.getNombre(),
                "Archivo eliminado",
                "Se eliminó: " + nombreArchivo
        );

        Log.d(TAG, "✓ Notificación enviada: Archivo eliminado - " + nombreArchivo);
    }

}