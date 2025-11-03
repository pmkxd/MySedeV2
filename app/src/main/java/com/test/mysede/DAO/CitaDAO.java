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
}