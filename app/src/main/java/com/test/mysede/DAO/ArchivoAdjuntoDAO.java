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
import com.test.mysede.model.ArchivoAdjunto;

import java.util.ArrayList;
import java.util.Map;

/**
 * DAO para gestionar los archivos adjuntos almacenados en Firestore.
 */
public class ArchivoAdjuntoDAO {

    private static final String COLLECTION = "archivos_adjuntos";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // este es el que se utiliza para guardar los datos en firestore
    public void saveArchivoAdjunto(ArchivoAdjunto archivoAdjunto) {
        if (archivoAdjunto == null) {
            return;
        }
        if (TextUtils.isEmpty(archivoAdjunto.getId())) {
            addArchivoAdjunto(archivoAdjunto);
        } else {
            updateArchivoAdjunto(archivoAdjunto);
        }
    }

    public void addArchivoAdjunto(ArchivoAdjunto archivoAdjunto) {
        Map<String, Object> data = FirestoreModelMapper.archivoAdjuntoToMap(archivoAdjunto);
        db.collection(COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        archivoAdjunto.setId(documentReference.getId());
                        Log.d(TAG, "Archivo adjunto registrado con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al registrar el archivo adjunto", e);
                    }
                });
    }

    public void updateArchivoAdjunto(ArchivoAdjunto archivoAdjunto) {
        if (TextUtils.isEmpty(archivoAdjunto.getId())) {
            Log.w(TAG, "No es posible actualizar un archivo adjunto sin ID");
            return;
        }
        Map<String, Object> data = FirestoreModelMapper.archivoAdjuntoToMap(archivoAdjunto);
        db.collection(COLLECTION)
                .document(archivoAdjunto.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Archivo adjunto actualizado correctamente");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar el archivo adjunto", e);
                    }
                });
    }

    public interface OnArchivosAdjuntosLoadedListener {
        void onArchivosAdjuntosLoaded(ArrayList<ArchivoAdjunto> archivosAdjuntos);

        void onError(Exception e);
    }

    public void getAllArchivosAdjuntos(OnArchivosAdjuntosLoadedListener listener) {
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<ArchivoAdjunto> archivosAdjuntos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            ArchivoAdjunto archivoAdjunto = FirestoreModelMapper.archivoAdjuntoFromMap(data);
                            if (archivoAdjunto != null) {
                                archivoAdjunto.setId(document.getId());
                                archivosAdjuntos.add(archivoAdjunto);
                            }
                        }
                        listener.onArchivosAdjuntosLoaded(archivosAdjuntos);
                    } else {
                        Log.w(TAG, "Error obteniendo archivos adjuntos", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }
}