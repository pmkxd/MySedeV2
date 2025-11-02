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
        if (cita == null || cita.getActividad() == null || TextUtils.isEmpty(cita.getActividad().getId())) {
            Log.w(TAG, "La cita debe estar asociada a una actividad con ID para poder guardarse");
            return;
        }
        if (TextUtils.isEmpty(cita.getId())) {
            addCita(cita);
        } else {
            updateCita(cita);
        }
    }

    public void addCita(Cita cita) {
        Map<String, Object> data = FirestoreModelMapper.citaToMap(cita);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        cita.setId(documentReference.getId());
                        Log.d(TAG, "Cita registrada con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar la cita", e);
                    }
                });
    }

    public void updateCita(Cita cita) {
        if (TextUtils.isEmpty(cita.getId())) {
            Log.w(TAG, "No es posible actualizar una cita sin ID");
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar la cita", e);
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
}