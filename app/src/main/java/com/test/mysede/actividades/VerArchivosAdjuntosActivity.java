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

        if (!PermissionManager.tienePermiso(Permiso.VER_ACTIVIDADES)) {
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
            if (!PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD)) {
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
        boolean puedeRenombrar = PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD);
        boolean puedeEliminar = PermissionManager.tienePermiso(Permiso.ELIMINAR_ACTIVIDAD);

        for (ArchivoAdjunto archivo : actividad.getArchivosAdjuntos()) {
            View itemView = inflater.inflate(R.layout.item_archivo_adjunto_detalle, layoutArchivosAdjuntos, false);

            TextView nombre = itemView.findViewById(R.id.tvNombreArchivo);
            TextView tipo = itemView.findViewById(R.id.tvTipoArchivo);
            TextView tamano = itemView.findViewById(R.id.tvTamanoArchivo);
            MaterialButton btnVer = itemView.findViewById(R.id.btnVerArchivo);
            MaterialButton btnDescargar = itemView.findViewById(R.id.btnDescargarArchivo);
            MaterialButton btnRenombrar = itemView.findViewById(R.id.btnRenombrarArchivo);
            MaterialButton btnEliminar = itemView.findViewById(R.id.btnEliminarArchivo);

            nombre.setText("📄 " + archivo.getNombre());
            String tipoDescripcion = archivo.getTipo() != null ? archivo.getTipo() : getString(R.string.ver_archivos_adjuntos_tipo_desconocido);
            tipo.setText(getString(R.string.ver_archivos_adjuntos_tipo_formato, tipoDescripcion));
            tamano.setText("Tamaño: " + formatearTamano(archivo.getTamaño()));

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

    private void mostrarDialogoRenombrarArchivo(ArchivoAdjunto archivo) {
        if (!PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD)) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_permission_add, Toast.LENGTH_SHORT).show();
            return;
        }
        if (archivo == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.editar);
        final EditText input = new EditText(this);
        String extension = obtenerExtension(archivo.getNombre());
        String nombreBase = archivo.getNombre();
        if (!TextUtils.isEmpty(extension) && nombreBase.endsWith(extension)) {
            nombreBase = nombreBase.substring(0, nombreBase.length() - extension.length());
        }
        input.setText(nombreBase);
        input.setSelection(nombreBase.length());
        builder.setView(input);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevoNombre = input.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                input.setError(getString(R.string.ver_archivos_adjuntos_error_nombre));
                return;
            }
            String extensionActual = obtenerExtension(archivo.getNombre());
            if (!TextUtils.isEmpty(extensionActual) && !nuevoNombre.endsWith(extensionActual)) {
                nuevoNombre += extensionActual;
            }
            dialog.dismiss();
            ejecutarRenombrarArchivo(archivo, nuevoNombre);
        }));
        dialog.show();
    }

    private void ejecutarRenombrarArchivo(ArchivoAdjunto archivo, String nombreFinal) {
        if (archivo == null || TextUtils.isEmpty(archivo.getId())) {
            Toast.makeText(this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            return;
        }
        ArchivoAdjunto copia = clonarArchivo(archivo);
        copia.setNombre(nombreFinal);
        archivoAdjuntoDAO.updateArchivoAdjunto(copia, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                archivo.setNombre(nombreFinal);
                actualizarArchivoEnActividad(archivo, copia);
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_rename_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarArchivoEnActividad(ArchivoAdjunto original, ArchivoAdjunto actualizado) {
        if (actividad == null || actividad.getArchivosAdjuntos() == null) {
            return;
        }
        List<ArchivoAdjunto> actuales = new ArrayList<>(actividad.getArchivosAdjuntos());
        for (int i = 0; i < actuales.size(); i++) {
            if (coincideArchivo(actuales.get(i), original)) {
                actuales.set(i, actualizado);
                break;
            }
        }
        actividad.setArchivosAdjuntos(actuales);
        actividadDAO.updateActividad(actividad, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                mostrarArchivosAdjuntos();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_error_general, Toast.LENGTH_SHORT).show();
                cargarActividad();
            }
        });
    }

    private void mostrarDialogoEliminarArchivo(ArchivoAdjunto archivo) {
        if (!PermissionManager.tienePermiso(Permiso.ELIMINAR_ACTIVIDAD)) {
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
        actuales.remove(encontrado);
        actividad.setArchivosAdjuntos(actuales);
        actividadDAO.updateActividad(actividad, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(VerArchivosAdjuntosActivity.this, R.string.ver_archivos_adjuntos_delete_success, Toast.LENGTH_SHORT).show();
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
        boolean puedeEditar = PermissionManager.tienePermiso(Permiso.EDITAR_ACTIVIDAD);
        boolean habilitado = puedeEditar && !bloqueadoPorCarga;
        btnAgregarArchivos.setEnabled(habilitado);
        btnAgregarArchivos.setAlpha(habilitado ? 1f : 0.5f);
    }
}