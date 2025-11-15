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
import com.test.mysede.model.TipoActividad;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para gestionar los tipos de actividad disponibles en la sede.
 */
public class TipoActividadDAO {

    private static final String COLLECTION = "tiposActividad";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveTipoActividad(TipoActividad tipoActividad) {
        saveTipoActividad(tipoActividad, null);
    }

    public void saveTipoActividad(TipoActividad tipoActividad, @Nullable FirestoreOperationCallback callback) {
        if (tipoActividad == null) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El tipo de actividad no puede ser nulo"));
            }
            return;
        }
        if (TextUtils.isEmpty(tipoActividad.getId())) {
            addTipoActividad(tipoActividad, callback);
        } else {
            updateTipoActividad(tipoActividad, callback);
        }
    }

    public void addTipoActividad(TipoActividad tipoActividad) {
        addTipoActividad(tipoActividad, null);
    }

    private void addTipoActividad(TipoActividad tipoActividad, @Nullable FirestoreOperationCallback callback) {
        // Validar campos obligatorios
        Exception validationError = validarTipoActividad(tipoActividad);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        Map<String, Object> data = FirestoreModelMapper.tipoActividadToMap(tipoActividad);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        tipoActividad.setId(documentReference.getId());
                        Log.d(TAG, "Tipo de actividad registrado con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el tipo de actividad", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void updateTipoActividad(TipoActividad tipoActividad) {
        updateTipoActividad(tipoActividad, null);
    }

    private void updateTipoActividad(TipoActividad tipoActividad, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(tipoActividad.getId())) {
            Log.w(TAG, "No es posible actualizar un tipo de actividad sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El tipo de actividad debe tener un ID válido"));
            }
            return;
        }

        // Validar campos obligatorios
        Exception validationError = validarTipoActividad(tipoActividad);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        Map<String, Object> data = FirestoreModelMapper.tipoActividadToMap(tipoActividad);
        db.collection(COLLECTION)
                .document(tipoActividad.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Tipo de actividad actualizado correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el tipo de actividad", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void deleteTipoActividad(TipoActividad tipoActividad) {
        deleteTipoActividad(tipoActividad, null);
    }

    public void deleteTipoActividad(TipoActividad tipoActividad, @Nullable FirestoreOperationCallback callback) {
        if (tipoActividad == null || TextUtils.isEmpty(tipoActividad.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El tipo de actividad debe tener un ID para ser eliminado"));
            }
            return;
        }
        db.collection(COLLECTION)
                .document(tipoActividad.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Tipo de actividad eliminado correctamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar el tipo de actividad", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public interface OnTiposActividadLoadedListener {
        void onTiposActividadLoaded(ArrayList<TipoActividad> tiposActividad);

        void onError(Exception e);
    }

    public void getAllTiposActividad(OnTiposActividadLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<TipoActividad> tiposActividad = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            TipoActividad tipoActividad = FirestoreModelMapper.tipoActividadFromMap(data);
                            if (tipoActividad != null) {
                                tipoActividad.setId(document.getId());
                                tiposActividad.add(tipoActividad);
                            }
                        }
                        listener.onTiposActividadLoaded(tiposActividad);
                    } else {
                        Log.w(TAG, "Error obteniendo tipos de actividad", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    // ============ MÉTODOS DE VALIDACIÓN ============

    /**
     * Valida que un tipo de actividad tenga todos los campos obligatorios
     * @param tipoActividad El tipo de actividad a validar
     * @return null si es válido, o una Exception con el mensaje de error
     */
    private Exception validarTipoActividad(TipoActividad tipoActividad) {
        if (tipoActividad == null) {
            return new IllegalArgumentException("El tipo de actividad no puede ser nulo");
        }

        if (TextUtils.isEmpty(tipoActividad.getNombre()) || tipoActividad.getNombre().trim().length() < 3) {
            return new IllegalArgumentException("El nombre del tipo de actividad es obligatorio y debe tener al menos 3 caracteres");
        }

        return null;
    }
}