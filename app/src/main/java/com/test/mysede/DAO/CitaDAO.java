package com.test.mysede.DAO;

import static android.content.ContentValues.TAG;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para manejar las citas asociadas a una actividad.
 */
public class CitaDAO {

    private static final String COLLECTION = "citas";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveCita(Cita cita) {
        saveCita(cita, null);
    }

    public void saveCita(Cita cita, @Nullable FirestoreOperationCallback callback) {
        if (cita == null || cita.getActividad() == null || TextUtils.isEmpty(cita.getActividad().getId())) {
            Log.w(TAG, "La cita debe estar asociada a una actividad con ID para poder guardarse");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La cita debe tener una actividad válida"));
            }
            return;
        }
        if (TextUtils.isEmpty(cita.getId())) {
            addCita(cita, callback);
        } else {
            updateCita(cita, callback);
        }
    }

    public void addCita(Cita cita) {
        addCita(cita, null);
    }

    private void addCita(Cita cita, @Nullable FirestoreOperationCallback callback) {
        // Validar campos obligatorios antes de insertar
        Exception validationError = validarCita(cita);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        // Verificar duplicados antes de insertar
        verificarCitaDuplicada(cita, new OnCitaDuplicadaListener() {
            @Override
            public void onResultado(boolean esDuplicada) {
                if (esDuplicada) {
                    if (callback != null) {
                        callback.onFailure(new IllegalStateException("Ya existe una cita para esta actividad en la misma fecha, hora y lugar"));
                    }
                    return;
                }

                // Si no es duplicada, proceder con la inserción
                Map<String, Object> data = FirestoreModelMapper.citaToMap(cita);
                db.collection(COLLECTION)
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                cita.setId(documentReference.getId());
                                Log.d(TAG, "Cita registrada con ID: " + documentReference.getId());
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error al registrar la cita", e);
                                if (callback != null) {
                                    callback.onFailure(e);
                                }
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public void updateCita(Cita cita) {
        updateCita(cita, null);
    }

    private void updateCita(Cita cita, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(cita.getId())) {
            Log.w(TAG, "No es posible actualizar una cita sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La cita debe tener un ID válido"));
            }
            return;
        }

        // Validar campos obligatorios antes de actualizar
        Exception validationError = validarCita(cita);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        Map<String, Object> data = FirestoreModelMapper.citaToMap(cita);
        db.collection(COLLECTION)
                .document(cita.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Cita actualizada correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar la cita", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public interface OnCitasLoadedListener {
        void onCitasLoaded(ArrayList<Cita> citas);

        void onError(Exception e);
    }

    public void getCitasPorActividad(Actividad actividad, OnCitasLoadedListener listener) {
        if (actividad == null || TextUtils.isEmpty(actividad.getId())) {
            Log.w(TAG, "Se requiere una actividad con ID para obtener sus citas");
            return;
        }
        db.collection(COLLECTION)
                .whereEqualTo("actividadId", actividad.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        ArrayList<Cita> citas = new ArrayList<>();
                        actividad.limpiarCitas();
                        for (QueryDocumentSnapshot document : snapshot) {
                            Map<String, Object> data = document.getData();
                            Cita cita = FirestoreModelMapper.citaFromMap(data, actividad);
                            if (cita != null) {
                                cita.setId(document.getId());
                                citas.add(cita);
                            }
                        }
                        listener.onCitasLoaded(citas);
                    } else {
                        Log.w(TAG, "Error obteniendo citas", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    public void deleteCitasPorActividad(Actividad actividad, @Nullable FirestoreOperationCallback callback) {
        if (actividad == null || TextUtils.isEmpty(actividad.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La actividad debe tener un ID válido"));
            }
            return;
        }
        db.collection(COLLECTION)
                .whereEqualTo("actividadId", actividad.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                        return;
                    }
                    final int total = querySnapshot.size();
                    final java.util.concurrent.atomic.AtomicInteger pendientes = new java.util.concurrent.atomic.AtomicInteger(total);
                    final java.util.concurrent.atomic.AtomicBoolean errorReportado = new java.util.concurrent.atomic.AtomicBoolean(false);
                    querySnapshot.getDocuments().forEach(documentSnapshot ->
                            documentSnapshot.getReference()
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        if (pendientes.decrementAndGet() == 0 && !errorReportado.get() && callback != null) {
                                            callback.onSuccess();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error al eliminar cita", e);
                                        if (errorReportado.compareAndSet(false, true) && callback != null) {
                                            callback.onFailure(e);
                                        }
                                    }));
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error obteniendo citas para eliminar", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public void deleteCita(Cita cita, @Nullable FirestoreOperationCallback callback) {
        if (cita == null || TextUtils.isEmpty(cita.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La cita debe tener un ID válido"));
            }
            return;
        }
        db.collection(COLLECTION)
                .document(cita.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar la cita", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    // ============ MÉTODOS DE VALIDACIÓN ============

    /**
     * Valida que una cita tenga todos los campos obligatorios
     * @param cita La cita a validar
     * @return null si es válida, o una Exception con el mensaje de error
     */
    private Exception validarCita(Cita cita) {
        if (cita == null) {
            return new IllegalArgumentException("La cita no puede ser nula");
        }

        if (cita.getActividad() == null) {
            return new IllegalArgumentException("La actividad es obligatoria");
        }

        if (cita.getLugar() == null) {
            return new IllegalArgumentException("El lugar es obligatorio");
        }

        if (cita.getFecha() == null) {
            return new IllegalArgumentException("La fecha es obligatoria");
        }

        if (cita.getHora() == null) {
            return new IllegalArgumentException("La hora es obligatoria");
        }

        return null;
    }

    /**
     * Verifica si ya existe una cita con la misma actividad, fecha, hora y lugar
     */
    private interface OnCitaDuplicadaListener {
        void onResultado(boolean esDuplicada);
        void onError(Exception e);
    }

    private void verificarCitaDuplicada(Cita cita, OnCitaDuplicadaListener listener) {
        if (cita.getActividad() == null || TextUtils.isEmpty(cita.getActividad().getId())) {
            listener.onError(new IllegalArgumentException("La cita debe tener una actividad válida"));
            return;
        }

        Map<String, Object> citaMap = FirestoreModelMapper.citaToMap(cita);
        String fecha = (String) citaMap.get("fecha");
        String hora = (String) citaMap.get("hora");
        String lugarId = (String) citaMap.get("lugarId");

        db.collection(COLLECTION)
                .whereEqualTo("actividadId", cita.getActividad().getId())
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("hora", hora)
                .whereEqualTo("lugarId", lugarId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onResultado(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando cita duplicada", e);
                    listener.onError(e);
                });
    }

    /**
     * Interface para verificar conflictos de horario
     */
    public interface OnConflictoHorarioListener {
        void onResultado(boolean hayConflicto);
        void onError(Exception e);
    }

    /**
     * Verifica si existe conflicto de horario en un lugar específico
     * Un conflicto ocurre cuando hay otra cita en el mismo lugar, fecha y hora
     * @param lugarId ID del lugar a verificar
     * @param fecha Fecha en formato String
     * @param hora Hora en formato String
     * @param excludeActividadId ID de actividad a excluir (null para no excluir ninguna)
     */
    public void verificarConflictoHorario(String lugarId, String fecha, String hora,
                                          @Nullable String excludeActividadId,
                                          OnConflictoHorarioListener listener) {
        if (TextUtils.isEmpty(lugarId) || TextUtils.isEmpty(fecha) || TextUtils.isEmpty(hora)) {
            listener.onError(new IllegalArgumentException("Lugar, fecha y hora son obligatorios"));
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("lugarId", lugarId)
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("hora", hora)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hayConflicto = false;

                    // Si estamos editando una actividad existente, excluir sus citas
                    if (excludeActividadId != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String actividadId = document.getString("actividadId");
                            if (actividadId != null && !actividadId.equals(excludeActividadId)) {
                                hayConflicto = true;
                                break;
                            }
                        }
                    } else {
                        // Si no hay exclusión, cualquier cita existente es un conflicto
                        hayConflicto = !querySnapshot.isEmpty();
                    }

                    listener.onResultado(hayConflicto);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando conflicto de horario", e);
                    listener.onError(e);
                });
    }
}