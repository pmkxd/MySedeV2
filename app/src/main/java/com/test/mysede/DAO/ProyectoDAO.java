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
import com.test.mysede.model.Proyecto;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para administrar los proyectos disponibles en la sede.
 */
public class ProyectoDAO {

    private static final String COLLECTION = "proyectos";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveProyecto(Proyecto proyecto) {
        saveProyecto(proyecto, null);
    }

    public void saveProyecto(Proyecto proyecto, @Nullable FirestoreOperationCallback callback) {
        if (proyecto == null) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El proyecto no puede ser nulo"));
            }
            return;
        }
        if (TextUtils.isEmpty(proyecto.getId())) {
            addProyecto(proyecto, callback);
        } else {
            updateProyecto(proyecto, callback);
        }
    }

    public void addProyecto(Proyecto proyecto) {
        addProyecto(proyecto, null);
    }

    private void addProyecto(Proyecto proyecto, @Nullable FirestoreOperationCallback callback) {
        Map<String, Object> data = FirestoreModelMapper.proyectoToMap(proyecto);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        proyecto.setId(documentReference.getId());
                        Log.d(TAG, "Proyecto registrado con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el proyecto", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void updateProyecto(Proyecto proyecto) {
        updateProyecto(proyecto, null);
    }

    private void updateProyecto(Proyecto proyecto, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(proyecto.getId())) {
            Log.w(TAG, "No es posible actualizar un proyecto sin ID");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El proyecto debe tener un ID válido"));
            }
            return;
        }
        Map<String, Object> data = FirestoreModelMapper.proyectoToMap(proyecto);
        db.collection(COLLECTION)
                .document(proyecto.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Proyecto actualizado correctamente");
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el proyecto", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void deleteProyecto(Proyecto proyecto) {
        deleteProyecto(proyecto, null);
    }

    public void deleteProyecto(Proyecto proyecto, @Nullable FirestoreOperationCallback callback) {
        if (proyecto == null || TextUtils.isEmpty(proyecto.getId())) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("El proyecto debe tener un ID para ser eliminado"));
            }
            return;
        }
        db.collection(COLLECTION)
                .document(proyecto.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Proyecto eliminado correctamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar el proyecto", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public interface OnProyectosLoadedListener {
        void onProyectosLoaded(ArrayList<Proyecto> proyectos);

        void onError(Exception e);
    }

    public void getAllProyectos(OnProyectosLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Proyecto> proyectos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            Proyecto proyecto = FirestoreModelMapper.proyectoFromMap(data);
                            if (proyecto != null) {
                                proyecto.setId(document.getId());
                                proyectos.add(proyecto);
                            }
                        }
                        listener.onProyectosLoaded(proyectos);
                    } else {
                        Log.w(TAG, "Error obteniendo proyectos", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }
}