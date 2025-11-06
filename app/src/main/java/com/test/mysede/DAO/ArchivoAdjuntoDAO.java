package com.test.mysede.DAO;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.mysede.ArchivoAdjunto;

import java.util.HashMap;
import java.util.Map;

public class ArchivoAdjuntoDAO {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // Guardar metadatos en Firestore
    public Task<DocumentReference> guardarArchivo(ArchivoAdjunto archivo) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", archivo.getNombre());
        data.put("tipo", archivo.getTipo());
        data.put("tamaño", archivo.getTamaño());
        data.put("ubicacion", archivo.getUri().toString());

        // Ruta en Firestore: /archivosAdjuntos
        return db.collection("archivosAdjuntos").add(data);
    }

    // Subir archivo físico a Firebase Storage
    public UploadTask subirArchivo(@NonNull ArchivoAdjunto archivo) {
        StorageReference ref = storage.getReference()
                .child("archivos/" + archivo.getNombre());
        return ref.putFile(archivo.getUri());
    }
}
