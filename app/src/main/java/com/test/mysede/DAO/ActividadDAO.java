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
import com.test.mysede.model.Actividad;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para administrar las actividades en Cloud Firestore.
 */
public class ActividadDAO {

    private static final String COLLECTION = "actividades";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveActividad(Actividad actividad) {
        saveActividad(actividad, null);
    }

    public void saveActividad(Actividad actividad, @Nullable FirestoreOperationCallback callback) {
        if (actividad == null) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La actividad no puede ser nula"));
            }
            return;
        }
        if (TextUtils.isEmpty(actividad.getId())) {
            addActividad(actividad, callback);
        } else {
            updateActividad(actividad, callback);
        }
    }

    public void addActividad(Actividad actividad) {
        addActividad(actividad, null);
    }

    private void addActividad(Actividad actividad, @Nullable FirestoreOperationCallback callback) {
        // Validar campos obligatorios antes de insertar
        Exception validationError = validarActividad(actividad);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        Map<String, Object> data = FirestoreModelMapper.actividadToMap(actividad);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        actividad.setId(documentReference.getId());
                        Log.d(TAG, "Actividad registrada con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar la actividad", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void updateActividad(Actividad actividad) {
        updateActividad(actividad, null);
    }

    public void updateActividad(Actividad actividad, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(actividad.getId())) {
            Log.w(TAG, "No es posible actualizar una actividad sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La actividad debe tener un ID válido"));
            }
            return;
        }

        // Validar campos obligatorios antes de actualizar
        Exception validationError = validarActividad(actividad);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        Map<String, Object> data = FirestoreModelMapper.actividadToMap(actividad);
        db.collection(COLLECTION)
                .document(actividad.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Actividad actualizada correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar la actividad", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void deleteActividad(Actividad actividad, @Nullable FirestoreOperationCallback callback) {
        if (actividad == null || TextUtils.isEmpty(actividad.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("La actividad debe tener un ID para ser eliminada"));
            }
            return;
        }

        // Verificar integridad referencial: comprobar si tiene citas asociadas
        verificarCitasAsociadas(actividad.getId(), new OnCitasVerificadasListener() {
            @Override
            public void onCitasVerificadas(boolean tieneCitas) {
                if (tieneCitas) {
                    if (callback != null) {
                        callback.onFailure(new IllegalStateException("No se puede eliminar la actividad porque tiene citas asociadas. Elimine las citas primero."));
                    }
                    return;
                }

                // Si no tiene citas, proceder con la eliminación
                db.collection(COLLECTION)
                        .document(actividad.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Actividad eliminada correctamente");
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al eliminar la actividad", e);
                            if (callback != null) {
                                callback.onFailure(e);
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

    public interface OnActividadesLoadedListener {
        void onActividadesLoaded(ArrayList<Actividad> actividades);

        void onError(Exception e);
    }

    public interface OnActividadLoadedListener {
        void onActividadLoaded(@Nullable Actividad actividad);

        void onError(Exception e);
    }

    public void getAllActividades(OnActividadesLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Actividad> actividades = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> data = document.getData();
                                Actividad actividad = FirestoreModelMapper.actividadFromMap(data, document.getId());
                                if (actividad != null) {
                                    actividades.add(actividad);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "No fue posible convertir la actividad " + document.getId(), e);
                            }
                        }
                        listener.onActividadesLoaded(actividades);
                    } else {
                        Log.w(TAG, "Error obteniendo actividades", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    public void getActividadById(String actividadId, OnActividadLoadedListener listener) {
        if (TextUtils.isEmpty(actividadId)) {
            if (listener != null) {
                listener.onActividadLoaded(null);
            }
            return;
        }
        db.collection(COLLECTION)
                .document(actividadId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        try {
                            Actividad actividad = FirestoreModelMapper.actividadFromMap(data, documentSnapshot.getId());
                            if (listener != null) {
                                listener.onActividadLoaded(actividad);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "No fue posible convertir la actividad " + documentSnapshot.getId(), e);
                            if (listener != null) {
                                listener.onError(e);
                            }
                        }
                    } else {
                        if (listener != null) {
                            listener.onActividadLoaded(null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error obteniendo actividad", e);
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }

    // ============ MÉTODOS DE VALIDACIÓN ============

    /**
     * Valida que una actividad tenga todos los campos obligatorios
     * @param actividad La actividad a validar
     * @return null si es válida, o una Exception con el mensaje de error
     */
    private Exception validarActividad(Actividad actividad) {
        if (actividad == null) {
            return new IllegalArgumentException("La actividad no puede ser nula");
        }

        if (TextUtils.isEmpty(actividad.getNombre()) || actividad.getNombre().trim().length() < 3) {
            return new IllegalArgumentException("El nombre de la actividad es obligatorio y debe tener al menos 3 caracteres");
        }

        if (actividad.getPeriodicidad() == null) {
            return new IllegalArgumentException("La periodicidad es obligatoria");
        }

        if (actividad.getProyecto() == null) {
            return new IllegalArgumentException("El proyecto es obligatorio");
        }

        if (actividad.getTiposActividad() == null || actividad.getTiposActividad().isEmpty()) {
            return new IllegalArgumentException("Debe seleccionar al menos un tipo de actividad");
        }

        if (actividad.getOferentes() == null || actividad.getOferentes().isEmpty()) {
            return new IllegalArgumentException("Debe seleccionar al menos un oferente");
        }

        if (actividad.getSocioComunitario() == null) {
            return new IllegalArgumentException("El socio comunitario es obligatorio");
        }

        return null;
    }

    /**
     * Verifica si una actividad tiene citas asociadas
     */
    private interface OnCitasVerificadasListener {
        void onCitasVerificadas(boolean tieneCitas);
        void onError(Exception e);
    }

    private void verificarCitasAsociadas(String actividadId, OnCitasVerificadasListener listener) {
        db.collection("citas")
                .whereEqualTo("actividadId", actividadId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onCitasVerificadas(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando citas asociadas", e);
                    listener.onError(e);
                });
    }
}