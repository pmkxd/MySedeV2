package com.test.mysede.DAO;

import static android.content.ContentValues.TAG;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

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
        if (tipoActividad == null) {
            return;
        }
        if (TextUtils.isEmpty(tipoActividad.getId())) {
            addTipoActividad(tipoActividad);
        } else {
            updateTipoActividad(tipoActividad);
        }
    }

    public void addTipoActividad(TipoActividad tipoActividad) {
        Map<String, Object> data = FirestoreModelMapper.tipoActividadToMap(tipoActividad);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        tipoActividad.setId(documentReference.getId());
                        Log.d(TAG, "Tipo de actividad registrado con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el tipo de actividad", e);
                    }
                });
    }

    public void updateTipoActividad(TipoActividad tipoActividad) {
        if (TextUtils.isEmpty(tipoActividad.getId())) {
            Log.w(TAG, "No es posible actualizar un tipo de actividad sin ID");
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el tipo de actividad", e);
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
}