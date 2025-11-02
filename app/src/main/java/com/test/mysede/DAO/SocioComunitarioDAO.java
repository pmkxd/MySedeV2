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
import com.test.mysede.model.SocioComunitario;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para gestionar los socios comunitarios y sus beneficiarios asociados.
 */
public class SocioComunitarioDAO {

    private static final String COLLECTION = "sociosComunitarios";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveSocioComunitario(SocioComunitario socioComunitario) {
        saveSocioComunitario(socioComunitario, null);
    }

    public void saveSocioComunitario(SocioComunitario socioComunitario, @Nullable FirestoreOperationCallback callback) {
        if (socioComunitario == null) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El socio comunitario no puede ser nulo"));
            }
            return;
        }
        if (TextUtils.isEmpty(socioComunitario.getId())) {
            addSocioComunitario(socioComunitario, callback);
        } else {
            updateSocioComunitario(socioComunitario, callback);
        }
    }

    public void addSocioComunitario(SocioComunitario socioComunitario) {
        addSocioComunitario(socioComunitario, null);
    }

    private void addSocioComunitario(SocioComunitario socioComunitario, @Nullable FirestoreOperationCallback callback) {
        Map<String, Object> data = FirestoreModelMapper.socioComunitarioToMap(socioComunitario);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        socioComunitario.setId(documentReference.getId());
                        Log.d(TAG, "Socio comunitario registrado con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el socio comunitario", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void updateSocioComunitario(SocioComunitario socioComunitario) {
        updateSocioComunitario(socioComunitario, null);
    }

    private void updateSocioComunitario(SocioComunitario socioComunitario, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(socioComunitario.getId())) {
            Log.w(TAG, "No es posible actualizar un socio comunitario sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El socio comunitario debe tener un ID válido"));
            }
            return;
        }
        Map<String, Object> data = FirestoreModelMapper.socioComunitarioToMap(socioComunitario);
        db.collection(COLLECTION)
                .document(socioComunitario.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Socio comunitario actualizado correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el socio comunitario", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void deleteSocioComunitario(SocioComunitario socioComunitario) {
        deleteSocioComunitario(socioComunitario, null);
    }

    public void deleteSocioComunitario(SocioComunitario socioComunitario, @Nullable FirestoreOperationCallback callback) {
        if (socioComunitario == null || TextUtils.isEmpty(socioComunitario.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El socio comunitario debe tener un ID para ser eliminado"));
            }
            return;
        }
        db.collection(COLLECTION)
                .document(socioComunitario.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Socio comunitario eliminado correctamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar el socio comunitario", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public interface OnSociosComunitariosLoadedListener {
        void onSociosComunitariosLoaded(ArrayList<SocioComunitario> socios);

        void onError(Exception e);
    }

    public void getAllSociosComunitarios(OnSociosComunitariosLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<SocioComunitario> socios = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            SocioComunitario socioComunitario = FirestoreModelMapper.socioComunitarioFromMap(data);
                            if (socioComunitario != null) {
                                socioComunitario.setId(document.getId());
                                socios.add(socioComunitario);
                            }
                        }
                        listener.onSociosComunitariosLoaded(socios);
                    } else {
                        Log.w(TAG, "Error obteniendo socios comunitarios", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }
}