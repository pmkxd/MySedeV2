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
        if (actividad == null) {
            return;
        }
        if (TextUtils.isEmpty(actividad.getId())) {
            addActividad(actividad);
        } else {
            updateActividad(actividad);
        }
    }

    public void addActividad(Actividad actividad) {
        Map<String, Object> data = FirestoreModelMapper.actividadToMap(actividad);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        actividad.setId(documentReference.getId());
                        Log.d(TAG, "Actividad registrada con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar la actividad", e);
                    }
                });
    }

    public void updateActividad(Actividad actividad) {
        if (TextUtils.isEmpty(actividad.getId())) {
            Log.w(TAG, "No es posible actualizar una actividad sin ID");
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar la actividad", e);
                    }
                });
    }

    public interface OnActividadesLoadedListener {
        void onActividadesLoaded(ArrayList<Actividad> actividades);

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
}