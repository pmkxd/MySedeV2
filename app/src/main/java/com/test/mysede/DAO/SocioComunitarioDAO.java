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
import com.test.mysede.model.SocioComunitario;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para gestionar los socios comunitarios y sus beneficiarios asociados.
 */
public class SocioComunitarioDAO {

    private static final String COLLECTION = "socios_comutarios";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveSocioComunitario(SocioComunitario socioComunitario) {
        if (socioComunitario == null) {
            return;
        }
        if (TextUtils.isEmpty(socioComunitario.getId())) {
            addSocioComunitario(socioComunitario);
        } else {
            updateSocioComunitario(socioComunitario);
        }
    }

    public void addSocioComunitario(SocioComunitario socioComunitario) {
        Map<String, Object> data = FirestoreModelMapper.socioComunitarioToMap(socioComunitario);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        socioComunitario.setId(documentReference.getId());
                        Log.d(TAG, "Socio comunitario registrado con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el socio comunitario", e);
                    }
                });
    }

    public void updateSocioComunitario(SocioComunitario socioComunitario) {
        if (TextUtils.isEmpty(socioComunitario.getId())) {
            Log.w(TAG, "No es posible actualizar un socio comunitario sin ID");
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el socio comunitario", e);
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