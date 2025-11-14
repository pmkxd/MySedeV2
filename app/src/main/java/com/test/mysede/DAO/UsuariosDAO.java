package com.test.mysede.DAO;

import static android.content.ContentValues.TAG;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        setNewUsuario(usuario, null);
    }

    private void setNewUsuario(Usuario usuario, @Nullable FirestoreOperationCallback callback) {
        // Validar campos obligatorios
        Exception validationError = validarUsuario(usuario);
        if (validationError != null) {
            Log.w(TAG, "Error de validación: " + validationError.getMessage());
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        // Verificar duplicados (email y RUT)
        verificarEmailDuplicado(usuario.getEmail(), null, new OnDuplicadoListener() {
            @Override
            public void onResultado(boolean esDuplicado) {
                if (esDuplicado) {
                    Exception error = new IllegalStateException("Ya existe un usuario con este email");
                    Log.w(TAG, error.getMessage());
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                    return;
                }

                // Verificar RUT duplicado si existe
                if (!TextUtils.isEmpty(usuario.getRut())) {
                    verificarRutDuplicado(usuario.getRut(), null, new OnDuplicadoListener() {
                        @Override
                        public void onResultado(boolean esDuplicado) {
                            if (esDuplicado) {
                                Exception error = new IllegalStateException("Ya existe un usuario con este RUT");
                                Log.w(TAG, error.getMessage());
                                if (callback != null) {
                                    callback.onFailure(error);
                                }
                                return;
                            }

                            // Si no hay duplicados, crear el usuario
                            crearUsuarioEnFirestore(usuario, callback);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
                } else {
                    // Si no hay RUT, crear directamente
                    crearUsuarioEnFirestore(usuario, callback);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    private void crearUsuarioEnFirestore(Usuario usuario, @Nullable FirestoreOperationCallback callback) {
        Map<String, Object> usuario_nuevo = new HashMap<>();
        usuario_nuevo.put("nombre", usuario.getNombre());
        usuario_nuevo.put("email", usuario.getEmail());
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
                        usuario.setId(documentReference.getId());
                        Log.d(TAG, "Usuario creado con ID: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al crear usuario", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    // Actualizar usuario existente
    private void updateUsuario(Usuario usuario) {
        updateUsuario(usuario, null);
    }

    private void updateUsuario(Usuario usuario, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(usuario.getId())) {
            Exception error = new IllegalArgumentException("El usuario debe tener un ID válido para ser actualizado");
            Log.w(TAG, error.getMessage());
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        // Validar campos obligatorios
        Exception validationError = validarUsuario(usuario);
        if (validationError != null) {
            Log.w(TAG, "Error de validación: " + validationError.getMessage());
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        // Verificar email duplicado (excluyendo el usuario actual)
        verificarEmailDuplicado(usuario.getEmail(), usuario.getId(), new OnDuplicadoListener() {
            @Override
            public void onResultado(boolean esDuplicado) {
                if (esDuplicado) {
                    Exception error = new IllegalStateException("Ya existe otro usuario con este email");
                    Log.w(TAG, error.getMessage());
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                    return;
                }

                // Verificar RUT duplicado si existe
                if (!TextUtils.isEmpty(usuario.getRut())) {
                    verificarRutDuplicado(usuario.getRut(), usuario.getId(), new OnDuplicadoListener() {
                        @Override
                        public void onResultado(boolean esDuplicado) {
                            if (esDuplicado) {
                                Exception error = new IllegalStateException("Ya existe otro usuario con este RUT");
                                Log.w(TAG, error.getMessage());
                                if (callback != null) {
                                    callback.onFailure(error);
                                }
                                return;
                            }

                            // Si no hay duplicados, actualizar el usuario
                            actualizarUsuarioEnFirestore(usuario, callback);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
                } else {
                    // Si no hay RUT, actualizar directamente
                    actualizarUsuarioEnFirestore(usuario, callback);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    private void actualizarUsuarioEnFirestore(Usuario usuario, @Nullable FirestoreOperationCallback callback) {
        Map<String, Object> usuario_mod = new HashMap<>();
        usuario_mod.put("nombre", usuario.getNombre());
        usuario_mod.put("email", usuario.getEmail());
        usuario_mod.put("rol", usuario.getRol() != null ? usuario.getRol().name() : null);
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
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al actualizar usuario", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
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
        deleteUsuario(id, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                if (onSuccess != null) {
                    onSuccess.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                if (onFailure != null) {
                    onFailure.onFailure(exception);
                }
            }
        });
    }

    public void deleteUsuario(String id, @Nullable FirestoreOperationCallback callback) {
        if (TextUtils.isEmpty(id)) {
            Exception error = new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío");
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        // Primero obtener el usuario para verificar si es administrador
        getUsuarioById(id, new OnUsuarioLoadedListener() {
            @Override
            public void onUsuarioLoaded(Usuario usuario) {
                if (usuario == null) {
                    Exception error = new IllegalArgumentException("Usuario no encontrado");
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                    return;
                }

                // Si es administrador, verificar que no sea el último
                if (usuario.getRol() == Rol.ADMINISTRADOR) {
                    verificarUltimoAdmin(id, new OnUltimoAdminListener() {
                        @Override
                        public void onResultado(boolean esUltimoAdmin) {
                            if (esUltimoAdmin) {
                                Exception error = new IllegalStateException("No se puede eliminar el último administrador del sistema");
                                if (callback != null) {
                                    callback.onFailure(error);
                                }
                                return;
                            }

                            // Si no es el último admin, proceder con la eliminación
                            eliminarUsuarioDeFirestore(id, callback);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
                } else {
                    // Si no es administrador, eliminar directamente
                    eliminarUsuarioDeFirestore(id, callback);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    private void eliminarUsuarioDeFirestore(String id, @Nullable FirestoreOperationCallback callback) {
        db.collection("usuarios")
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Usuario eliminado exitosamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar usuario", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
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

    // ============ MÉTODOS DE VALIDACIÓN ============

    /**
     * Valida que un usuario tenga todos los campos obligatorios y formato correcto
     * @param usuario El usuario a validar
     * @return null si es válido, o una Exception con el mensaje de error
     */
    private Exception validarUsuario(Usuario usuario) {
        if (usuario == null) {
            return new IllegalArgumentException("El usuario no puede ser nulo");
        }

        // Validar nombre
        if (TextUtils.isEmpty(usuario.getNombre()) || usuario.getNombre().trim().length() < 3) {
            return new IllegalArgumentException("El nombre es obligatorio y debe tener al menos 3 caracteres");
        }

        // Validar email
        if (TextUtils.isEmpty(usuario.getEmail())) {
            return new IllegalArgumentException("El email es obligatorio");
        }

        // Validar formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(usuario.getEmail()).matches()) {
            return new IllegalArgumentException("El formato del email no es válido");
        }

        // Validar rol
        if (usuario.getRol() == null) {
            return new IllegalArgumentException("El rol es obligatorio");
        }

        return null;
    }

    /**
     * Interface para verificar duplicados
     */
    private interface OnDuplicadoListener {
        void onResultado(boolean esDuplicado);
        void onError(Exception e);
    }

    /**
     * Verifica si ya existe un usuario con el mismo email
     * @param email Email a verificar
     * @param excludeId ID del usuario a excluir (para updates), o null
     */
    private void verificarEmailDuplicado(String email, @Nullable String excludeId, OnDuplicadoListener listener) {
        if (TextUtils.isEmpty(email)) {
            listener.onError(new IllegalArgumentException("El email no puede ser nulo o vacío"));
            return;
        }

        db.collection("usuarios")
                .whereEqualTo("email", email.toLowerCase().trim())
                .limit(2)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean esDuplicado = false;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Si estamos excluyendo un ID (update), verificar que no sea el mismo documento
                        if (excludeId == null || !document.getId().equals(excludeId)) {
                            esDuplicado = true;
                            break;
                        }
                    }
                    listener.onResultado(esDuplicado);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando email duplicado", e);
                    listener.onError(e);
                });
    }

    /**
     * Verifica si ya existe un usuario con el mismo RUT
     * @param rut RUT a verificar
     * @param excludeId ID del usuario a excluir (para updates), o null
     */
    private void verificarRutDuplicado(String rut, @Nullable String excludeId, OnDuplicadoListener listener) {
        if (TextUtils.isEmpty(rut)) {
            listener.onResultado(false);
            return;
        }

        db.collection("usuarios")
                .whereEqualTo("rut", rut.trim())
                .limit(2)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean esDuplicado = false;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Si estamos excluyendo un ID (update), verificar que no sea el mismo documento
                        if (excludeId == null || !document.getId().equals(excludeId)) {
                            esDuplicado = true;
                            break;
                        }
                    }
                    listener.onResultado(esDuplicado);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando RUT duplicado", e);
                    listener.onError(e);
                });
    }

    /**
     * Interface para verificar último admin
     */
    private interface OnUltimoAdminListener {
        void onResultado(boolean esUltimoAdmin);
        void onError(Exception e);
    }

    /**
     * Verifica si un usuario es el último administrador del sistema
     * @param userId ID del usuario a verificar
     */
    private void verificarUltimoAdmin(String userId, OnUltimoAdminListener listener) {
        db.collection("usuarios")
                .whereEqualTo("rol", Rol.ADMINISTRADOR.name())
                .whereEqualTo("activo", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int adminsActivos = 0;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        adminsActivos++;
                        // Si hay más de un admin activo, no es el último
                        if (adminsActivos > 1) {
                            listener.onResultado(false);
                            return;
                        }
                    }
                    // Si solo hay 1 admin activo (o ninguno), es el último
                    listener.onResultado(adminsActivos <= 1);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando último admin", e);
                    listener.onError(e);
                });
    }
}