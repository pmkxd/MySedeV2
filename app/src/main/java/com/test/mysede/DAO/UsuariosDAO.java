package com.test.mysede.DAO;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.Rol;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UsuariosDAO {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Guardar o actualizar usuario
    public void saveUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            setNewUsuario(usuario);
        } else {
            updateUsuario(usuario);
        }
    }

    // Crear nuevo usuario
    private void setNewUsuario(Usuario usuario) {
        Map<String, Object> usuario_nuevo = new HashMap<>();
        usuario_nuevo.put("nombre", usuario.getNombre());
        usuario_nuevo.put("email", usuario.getEmail());
        usuario_nuevo.put("contrasena_hash", usuario.getContrasena_hash());
        usuario_nuevo.put("rut", usuario.getRut());
        usuario_nuevo.put("rol", usuario.getRol() != null ? usuario.getRol().name() : null);
        usuario_nuevo.put("permisos", convertirPermisosALista(usuario.getPermisos()));
        usuario_nuevo.put("activo", usuario.isActivo());
        usuario_nuevo.put("fechaCreacion", usuario.getFechaCreacion());
        usuario_nuevo.put("ultimoAcceso", usuario.getUltimoAcceso());

        db.collection("usuarios")
                .add(usuario_nuevo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Usuario creado con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al crear usuario", e);
                    }
                });
    }

    // Actualizar usuario existente
    private void updateUsuario(Usuario usuario) {
        Map<String, Object> usuario_mod = new HashMap<>();
        usuario_mod.put("nombre", usuario.getNombre());
        usuario_mod.put("email", usuario.getEmail());
        usuario_mod.put("rol", usuario.getRol() != null ? usuario.getRol().name() : null);
        usuario_mod.put("contrasena_hash", usuario.getContrasena_hash());
        usuario_mod.put("rut", usuario.getRut());
        usuario_mod.put("permisos", convertirPermisosALista(usuario.getPermisos()));
        usuario_mod.put("activo", usuario.isActivo());
        usuario_mod.put("fechaCreacion", usuario.getFechaCreacion());
        usuario_mod.put("ultimoAcceso", usuario.getUltimoAcceso());

        db.collection("usuarios").document(usuario.getId())
                .set(usuario_mod)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Usuario actualizado exitosamente");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar usuario", e);
                    }
                });
    }

    // Interface para callback
    public interface OnUsuariosLoadedListener {
        void onUsuariosLoaded(ArrayList<Usuario> usuarios);
        void onError(Exception e);
    }

    // Obtener todos los usuarios
    public void getAllUsuarios(OnUsuariosLoadedListener listener) {
        db.collection("usuarios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Usuario> usuariosList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Usuario usuario = convertirDocumentoAUsuario(document);
                                if (usuario != null) {
                                    usuariosList.add(usuario);
                                    Log.d(TAG, "Usuario cargado: " + usuario.getNombre());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al convertir usuario: " + document.getId(), e);
                            }
                        }
                        // Llamar al listener con la lista completa
                        listener.onUsuariosLoaded(usuariosList);
                    } else {
                        Log.w(TAG, "Error al obtener usuarios", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    // Interface para obtener un solo usuario
    public interface OnUsuarioLoadedListener {
        void onUsuarioLoaded(Usuario usuario);
        void onError(Exception e);
    }

    // Obtener usuario por ID
    public void getUsuarioById(String id, OnUsuarioLoadedListener listener) {
        db.collection("usuarios")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Usuario usuario = convertirDocumentoAUsuario(documentSnapshot);
                            listener.onUsuarioLoaded(usuario);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al convertir usuario", e);
                            listener.onError(e);
                        }
                    } else {
                        listener.onError(new Exception("Usuario no encontrado"));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    // Eliminar usuario
    public void deleteUsuario(String id, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("usuarios")
                .document(id)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Convierte un DocumentSnapshot de Firebase a un objeto Usuario
     */
    private Usuario convertirDocumentoAUsuario(com.google.firebase.firestore.DocumentSnapshot document) {
        Usuario usuario = new Usuario();

        // ID del documento
        usuario.setId(document.getId());

        // Datos básicos
        usuario.setNombre(document.getString("nombre"));
        usuario.setEmail(document.getString("email"));
        usuario.setRut(document.getString("rut"));
        usuario.setContrasena_hash(document.getString("contrasena_hash"));

        // Estado
        Boolean activo = document.getBoolean("activo");
        usuario.setActivo(activo != null ? activo : true);

        // Fechas
        Long fechaCreacion = document.getLong("fechaCreacion");
        usuario.setFechaCreacion(fechaCreacion != null ? fechaCreacion : System.currentTimeMillis());

        Long ultimoAcceso = document.getLong("ultimoAcceso");
        usuario.setUltimoAcceso(ultimoAcceso != null ? ultimoAcceso : System.currentTimeMillis());

        // Rol
        String rolString = document.getString("rol");
        if (rolString != null && !rolString.isEmpty()) {
            try {
                usuario.setRol(Rol.valueOf(rolString));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Rol inválido: " + rolString, e);
                usuario.setRol(Rol.ORGANIZADOR_ACTIVIDADES); // Rol por defecto
            }
        } else {
            usuario.setRol(Rol.ORGANIZADOR_ACTIVIDADES); // Rol por defecto
        }

        // Permisos
        List<String> permisosString = (List<String>) document.get("permisos");
        Set<Permiso> permisos = convertirListaAPermisos(permisosString);
        usuario.setPermisos(permisos);

        return usuario;
    }

    /**
     * Convierte una lista de strings a un Set de Permisos
     */
    private Set<Permiso> convertirListaAPermisos(List<String> permisosString) {
        Set<Permiso> permisos = new HashSet<>();

        if (permisosString != null) {
            for (String permisoStr : permisosString) {
                try {
                    Permiso permiso = Permiso.valueOf(permisoStr);
                    permisos.add(permiso);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Permiso inválido ignorado: " + permisoStr);
                }
            }
        }

        return permisos;
    }

    /**
     * Convierte un Set de Permisos a una lista de strings
     */
    private List<String> convertirPermisosALista(Set<Permiso> permisos) {
        List<String> permisosString = new ArrayList<>();

        if (permisos != null) {
            for (Permiso permiso : permisos) {
                permisosString.add(permiso.name());
            }
        }

        return permisosString;
    }
}