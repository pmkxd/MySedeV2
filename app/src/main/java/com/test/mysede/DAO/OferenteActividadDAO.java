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
import com.test.mysede.model.OferenteActividad;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para gestionar los oferentes de actividades.
 */
public class OferenteActividadDAO {

    private static final String COLLECTION = "oferentes";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveOferente(OferenteActividad oferenteActividad) {
        saveOferente(oferenteActividad, null);
    }

    public void saveOferente(OferenteActividad oferenteActividad, @Nullable FirestoreOperationCallback callback) {
        if (oferenteActividad == null) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El oferente no puede ser nulo"));
            }
            return;
        }
        if (TextUtils.isEmpty(oferenteActividad.getId())) {
            addOferente(oferenteActividad, callback);
        } else {
            updateOferente(oferenteActividad, callback);
        }
    }

    public void addOferente(OferenteActividad oferenteActividad) {
        addOferente(oferenteActividad, null);
    }

    private void addOferente(OferenteActividad oferenteActividad, @Nullable FirestoreOperationCallback callback) {
        Map<String, Object> data = FirestoreModelMapper.oferenteToMap(oferenteActividad);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        oferenteActividad.setId(documentReference.getId());
                        Log.d(TAG, "Oferente registrado con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el oferente", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void updateOferente(OferenteActividad oferenteActividad) {
        updateOferente(oferenteActividad, null);
    }

    private void updateOferente(OferenteActividad oferenteActividad, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(oferenteActividad.getId())) {
            Log.w(TAG, "No es posible actualizar un oferente sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El oferente debe tener un ID válido"));
            }
            return;
        }
        Map<String, Object> data = FirestoreModelMapper.oferenteToMap(oferenteActividad);
        db.collection(COLLECTION)
                .document(oferenteActividad.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Oferente actualizado correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el oferente", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void deleteOferente(OferenteActividad oferenteActividad) {
        deleteOferente(oferenteActividad, null);
    }

    public void deleteOferente(OferenteActividad oferenteActividad, @Nullable FirestoreOperationCallback callback) {
        if (oferenteActividad == null || TextUtils.isEmpty(oferenteActividad.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El oferente debe tener un ID para ser eliminado"));
            }
            return;
        }
        db.collection(COLLECTION)
                .document(oferenteActividad.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Oferente eliminado correctamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar el oferente", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public interface OnOferentesLoadedListener {
        void onOferentesLoaded(ArrayList<OferenteActividad> oferentes);

        void onError(Exception e);
    }

    public void getAllOferentes(OnOferentesLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<OferenteActividad> oferentes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            OferenteActividad oferente = FirestoreModelMapper.oferenteFromMap(data);
                            if (oferente != null) {
                                oferente.setId(document.getId());
                                oferentes.add(oferente);
                            }
                        }
                        listener.onOferentesLoaded(oferentes);
                    } else {
                        Log.w(TAG, "Error obteniendo oferentes", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }
}
