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
import com.test.mysede.model.Lugar;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para gestionar los lugares disponibles para las actividades.
 */
public class LugarDAO {

    private static final String COLLECTION = "lugares";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveLugar(Lugar lugar) {
        saveLugar(lugar, null);
    }

    public void saveLugar(Lugar lugar, @Nullable FirestoreOperationCallback callback) {
        if (lugar == null) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El lugar no puede ser nulo"));
            }
            return;
        }
        if (TextUtils.isEmpty(lugar.getId())) {
            addLugar(lugar, callback);
        } else {
            updateLugar(lugar, callback);
        }
    }

    public void addLugar(Lugar lugar) {
        addLugar(lugar, null);
    }

    private void addLugar(Lugar lugar, @Nullable FirestoreOperationCallback callback) {
        Map<String, Object> data = FirestoreModelMapper.lugarToMap(lugar);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lugar.setId(documentReference.getId());
                        Log.d(TAG, "Lugar registrado con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el lugar", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void updateLugar(Lugar lugar) {
        updateLugar(lugar, null);
    }

    private void updateLugar(Lugar lugar, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(lugar.getId())) {
            Log.w(TAG, "No es posible actualizar un lugar sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El lugar debe tener un ID válido"));
            }
            return;
        }
        Map<String, Object> data = FirestoreModelMapper.lugarToMap(lugar);
        db.collection(COLLECTION)
                .document(lugar.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Lugar actualizado correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el lugar", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void deleteLugar(Lugar lugar) {
        deleteLugar(lugar, null);
    }

    public void deleteLugar(Lugar lugar, @Nullable FirestoreOperationCallback callback) {
        if (lugar == null || TextUtils.isEmpty(lugar.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El lugar debe tener un ID para ser eliminado"));
            }
            return;
        }
        db.collection(COLLECTION)
                .document(lugar.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Lugar eliminado correctamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar el lugar", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public interface OnLugaresLoadedListener {
        void onLugaresLoaded(ArrayList<Lugar> lugares);

        void onError(Exception e);
    }

    public void getAllLugares(OnLugaresLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Lugar> lugares = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            Lugar lugar = FirestoreModelMapper.lugarFromMap(data);
                            if (lugar != null) {
                                lugar.setId(document.getId());
                                lugares.add(lugar);
                            }
                        }
                        listener.onLugaresLoaded(lugares);
                    } else {
                        Log.w(TAG, "Error obteniendo lugares", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }
}