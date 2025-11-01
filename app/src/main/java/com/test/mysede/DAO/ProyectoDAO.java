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
        if (proyecto == null) {
            return;
        }
        if (TextUtils.isEmpty(proyecto.getId())) {
            addProyecto(proyecto);
        } else {
            updateProyecto(proyecto);
        }
    }

    public void addProyecto(Proyecto proyecto) {
        Map<String, Object> data = FirestoreModelMapper.proyectoToMap(proyecto);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        proyecto.setId(documentReference.getId());
                        Log.d(TAG, "Proyecto registrado con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el proyecto", e);
                    }
                });
    }

    public void updateProyecto(Proyecto proyecto) {
        if (TextUtils.isEmpty(proyecto.getId())) {
            Log.w(TAG, "No es posible actualizar un proyecto sin ID");
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el proyecto", e);
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