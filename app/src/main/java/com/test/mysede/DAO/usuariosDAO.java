package com.test.mysede.DAO;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class usuariosDAO {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private void setNewUsuario(Usuario usuario){
        Map<String, Object> usuario_nuevo = new HashMap<>();
        usuario_nuevo.put("nombre", usuario.getNombre());
        usuario_nuevo.put("email", usuario.getEmail());
        usuario_nuevo.put("contrasena_hash", usuario.getContrasena_hash());
        usuario_nuevo.put("rut", usuario.getRut());
        usuario_nuevo.put("rol", usuario.getRol());
        usuario_nuevo.put("permisos", usuario.getPermisos());
        usuario_nuevo.put("activo", usuario.isActivo());
        usuario_nuevo.put("fechaCreacion", usuario.getFechaCreacion());
        usuario_nuevo.put("ultimoAcceso", usuario.getUltimoAcceso());

        db.collection("usuarios")
                .add(usuario_nuevo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
    private void updateUsuario(Usuario usuario){
        Map<String, Object> usuario_mod = new HashMap<>();
        usuario_mod.put("nombre", usuario.getNombre());
        usuario_mod.put("email", usuario.getEmail());
        usuario_mod.put("rol", usuario.getRol());
        usuario_mod.put("contrasena_hash", usuario.getContrasena_hash());
        usuario_mod.put("rut", usuario.getRut());
        usuario_mod.put("permisos", usuario.getPermisos());
        usuario_mod.put("activo", usuario.isActivo());
        usuario_mod.put("fechaCreacion", usuario.getFechaCreacion());
        usuario_mod.put("ultimoAcceso", usuario.getUltimoAcceso());
        db.collection("cities").document(usuario.getId())
                .set(usuario_mod)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
    public interface OnUsuariosLoadedListener {
        void onUsuariosLoaded(ArrayList<Usuario> usuarios);
        void onError(Exception e);
    }
    public void getAllUsuarios(OnUsuariosLoadedListener listener) {
        db.collection("usuarios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Usuario> usuariosList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convierte cada documento en un objeto Usuario
                            Usuario usuario = document.toObject(Usuario.class);
                            // Asigna el ID del documento al objeto
                            usuario.setId(document.getId());
                            usuariosList.add(usuario);
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                        // Llama al método del listener con la lista completa
                        listener.onUsuariosLoaded(usuariosList);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        // Llama al método de error del listener
                        listener.onError(task.getException());
                    }
                });
    }
}
