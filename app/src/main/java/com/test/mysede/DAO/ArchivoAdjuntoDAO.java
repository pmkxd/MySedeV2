package com.test.mysede.DAO;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.mysede.model.ArchivoAdjunto;

import java.util.HashMap;
import java.util.Map;

public class ArchivoAdjuntoDAO {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Cuando se implemente el storage
    // private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // Guardar metadatos en Firestore
    public Task<DocumentReference> guardarArchivo(@NonNull ArchivoAdjunto archivo) {
        Map<String, Object> data = FirestoreModelMapper.archivoAdjuntoToMap(archivo);
        return db.collection("archivosAdjuntos").add(data);
    }

    // Subir archivo físico a Firebase Storage
}
