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
        // Validar campos obligatorios
        Exception validationError = validarProyecto(proyecto);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        // Verificar duplicados
        verificarNombreDuplicado(proyecto.getNombre(), null, new OnDuplicadoListener() {
            @Override
            public void onResultado(boolean esDuplicado) {
                if (esDuplicado) {
                    if (callback != null) {
                        callback.onFailure(new IllegalStateException("Ya existe un proyecto con este nombre"));
                    }
                    return;
                }

                // Si no es duplicado, proceder con la inserción
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

            @Override
            public void onError(Exception e) {
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

        // Validar campos obligatorios
        Exception validationError = validarProyecto(proyecto);
        if (validationError != null) {
            if (callback != null) {
                callback.onFailure(validationError);
            }
            return;
        }

        // Verificar nombre duplicado (excluyendo el proyecto actual)
        verificarNombreDuplicado(proyecto.getNombre(), proyecto.getId(), new OnDuplicadoListener() {
            @Override
            public void onResultado(boolean esDuplicado) {
                if (esDuplicado) {
                    if (callback != null) {
                        callback.onFailure(new IllegalStateException("Ya existe otro proyecto con este nombre"));
                    }
                    return;
                }

                // Si no hay duplicados, actualizar
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

            @Override
            public void onError(Exception e) {
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

        // Verificar integridad referencial: comprobar si tiene actividades asociadas
        verificarActividadesAsociadas(proyecto.getId(), new OnActividadesVerificadasListener() {
            @Override
            public void onActividadesVerificadas(boolean tieneActividades) {
                if (tieneActividades) {
                    if (callback != null) {
                        callback.onFailure(new IllegalStateException("No se puede eliminar el proyecto porque tiene actividades asociadas. Elimine o reasigne las actividades primero."));
                    }
                    return;
                }

                // Si no tiene actividades, proceder con la eliminación
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

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
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

    // ============ MÉTODOS DE VALIDACIÓN ============

    /**
     * Valida que un proyecto tenga todos los campos obligatorios
     * @param proyecto El proyecto a validar
     * @return null si es válido, o una Exception con el mensaje de error
     */
    private Exception validarProyecto(Proyecto proyecto) {
        if (proyecto == null) {
            return new IllegalArgumentException("El proyecto no puede ser nulo");
        }

        if (TextUtils.isEmpty(proyecto.getNombre()) || proyecto.getNombre().trim().length() < 3) {
            return new IllegalArgumentException("El nombre del proyecto es obligatorio y debe tener al menos 3 caracteres");
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
     * Verifica si ya existe un proyecto con el mismo nombre
     * @param nombre Nombre a verificar
     * @param excludeId ID del proyecto a excluir (para updates), o null
     */
    private void verificarNombreDuplicado(String nombre, @Nullable String excludeId, OnDuplicadoListener listener) {
        if (TextUtils.isEmpty(nombre)) {
            listener.onError(new IllegalArgumentException("El nombre no puede ser nulo o vacío"));
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo("nombre", nombre.trim())
                .limit(2)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean esDuplicado = false;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        if (excludeId == null || !document.getId().equals(excludeId)) {
                            esDuplicado = true;
                            break;
                        }
                    }
                    listener.onResultado(esDuplicado);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando nombre duplicado", e);
                    listener.onError(e);
                });
    }

    /**
     * Interface para verificar actividades asociadas
     */
    private interface OnActividadesVerificadasListener {
        void onActividadesVerificadas(boolean tieneActividades);
        void onError(Exception e);
    }

    /**
     * Verifica si un proyecto tiene actividades asociadas
     */
    private void verificarActividadesAsociadas(String proyectoId, OnActividadesVerificadasListener listener) {
        db.collection("actividades")
                .whereEqualTo("proyectoId", proyectoId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onActividadesVerificadas(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error verificando actividades asociadas", e);
                    listener.onError(e);
                });
    }
}