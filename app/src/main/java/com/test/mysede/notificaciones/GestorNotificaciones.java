package com.test.mysede.notificaciones;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Gestor centralizado para manejar todas las notificaciones de MySede
 * Notifica a TODOS los usuarios con permisos activos
 */
public class GestorNotificaciones {

    private static final String TAG = "GestorNotificaciones";
    private Context context;
    private FirebaseFirestore db;

    public GestorNotificaciones(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // ==================== NOTIFICACIONES DE ACTIVIDADES ====================

    /**
     * Notificar cuando se crea una nueva actividad
     */
    public void notificarNuevaActividad(String actividadId, String nombreActividad,
                                        String fecha, String hora) {
        String titulo = "Nueva actividad disponible";
        String mensaje = nombreActividad + " - " + fecha + " a las " + hora;

        enviarNotificacionInmediata(titulo, mensaje, "nuevo", actividadId, null);
    }

    /**
     * Notificar confirmación de inscripción
     */
    public void notificarConfirmacionInscripcion(String actividadId, String nombreActividad,
                                                 String fecha, String hora, String lugar) {
        String titulo = "Inscripción confirmada ✓";
        String mensaje = "Tu cupo está reservado para " + nombreActividad +
                " el " + fecha + " a las " + hora + " en " + lugar;

        enviarNotificacionInmediata(titulo, mensaje, "confirmacion", actividadId, null);
    }

    /**
     * Notificar cambio en una actividad
     */
    public void notificarCambioActividad(String actividadId, String nombreActividad,
                                         String tipoCambio, String detalle) {
        String titulo = "Cambio en actividad: " + tipoCambio;
        String mensaje = nombreActividad + " - " + detalle;

        enviarNotificacionInmediata(titulo, mensaje, "cambio", actividadId, null);
    }

    /**
     * Notificar cancelación de actividad
     */
    public void notificarCancelacionActividad(String actividadId, String nombreActividad,
                                              String motivo) {
        String titulo = "Actividad cancelada ⚠️";
        String mensaje = nombreActividad + " ha sido cancelada. " +
                (motivo != null ? "Motivo: " + motivo : "");

        enviarNotificacionInmediata(titulo, mensaje, "cancelacion", actividadId, null);
    }

    /**
     * Notificar cuando el cupo está lleno
     */
    public void notificarCupoLleno(String actividadId, String nombreActividad) {
        String titulo = "Cupos agotados";
        String mensaje = nombreActividad + " ya no tiene cupos disponibles";

        enviarNotificacionInmediata(titulo, mensaje, "general", actividadId, null);
    }

    /**
     * Notificar cuando hay cupos disponibles nuevamente
     */
    public void notificarCupoDisponible(String actividadId, String nombreActividad) {
        String titulo = "¡Cupo disponible!";
        String mensaje = "Hay un nuevo cupo disponible para " + nombreActividad;

        enviarNotificacionInmediata(titulo, mensaje, "general", actividadId, null);
    }

    // ==================== NOTIFICACIONES DE CITAS ====================

    /**
     * Programar todas las notificaciones para una cita
     * Incluye: aviso previo según diasAvisoPrevio, 24h antes, 2h antes
     */
    public void programarNotificacionesCita(String actividadId, String citaId,
                                            String nombreActividad, String lugar,
                                            long fechaHoraMillis, int diasAvisoPrevio) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String fecha = sdf.format(new Date(fechaHoraMillis));
        String hora = sdfHora.format(new Date(fechaHoraMillis));

        // 1. Notificación según diasAvisoPrevio
        if (diasAvisoPrevio > 0) {
            long momentoAviso = fechaHoraMillis - (diasAvisoPrevio * 24 * 60 * 60 * 1000L);
            String titulo = "Recordatorio de actividad";
            String mensaje = String.format("En %d día%s tienes %s - %s a las %s en %s",
                    diasAvisoPrevio,
                    diasAvisoPrevio > 1 ? "s" : "",
                    nombreActividad,
                    fecha,
                    hora,
                    lugar);

            programarNotificacion(titulo, mensaje, "recordatorio",
                    actividadId, citaId, momentoAviso);

            Log.d(TAG, "Programada notificación para " + diasAvisoPrevio + " días antes");
        }

        // 2. Notificación 24 horas antes
        long unDiaAntes = fechaHoraMillis - (24 * 60 * 60 * 1000L);
        if (unDiaAntes > System.currentTimeMillis()) {
            String titulo = "Mañana tienes una actividad";
            String mensaje = nombreActividad + " - " + hora + " en " + lugar;

            programarNotificacion(titulo, mensaje, "recordatorio",
                    actividadId, citaId, unDiaAntes);

            Log.d(TAG, "Programada notificación para 24h antes");
        }

        // 3. Notificación 2 horas antes
        long dosHorasAntes = fechaHoraMillis - (2 * 60 * 60 * 1000L);
        if (dosHorasAntes > System.currentTimeMillis()) {
            String titulo = "Tu actividad es en 2 horas ⏰";
            String mensaje = nombreActividad + " - " + hora + " en " + lugar;

            programarNotificacion(titulo, mensaje, "recordatorio",
                    actividadId, citaId, dosHorasAntes);

            Log.d(TAG, "Programada notificación para 2h antes");
        }

        // 4. Notificación 30 minutos antes (opcional)
        long mediaHoraAntes = fechaHoraMillis - (30 * 60 * 1000L);
        if (mediaHoraAntes > System.currentTimeMillis()) {
            String titulo = "¡Tu actividad comienza pronto! ⏰";
            String mensaje = nombreActividad + " en 30 minutos - " + lugar;

            programarNotificacion(titulo, mensaje, "recordatorio",
                    actividadId, citaId, mediaHoraAntes);

            Log.d(TAG, "Programada notificación para 30min antes");
        }
    }

    /**
     * Cancelar todas las notificaciones programadas de una cita
     */
    public void cancelarNotificacionesCita(String citaId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("cita_" + citaId);
        Log.d(TAG, "Canceladas notificaciones de cita: " + citaId);
    }

    // ==================== NOTIFICACIONES DE ARCHIVOS ====================

    /**
     * Notificar cuando se sube nuevo material
     */
    public void notificarNuevoMaterial(String actividadId, String nombreActividad,
                                       String nombreArchivo) {
        String titulo = "Nuevo material disponible 📄";
        String mensaje = "Se agregó " + nombreArchivo + " a " + nombreActividad;

        enviarNotificacionInmediata(titulo, mensaje, "general", actividadId, null);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtener todos los usuarios que deben recibir notificaciones
     */
    private void obtenerUsuariosConNotificaciones(OnUsuariosLoadedListener listener) {
        // Enviar a TODOS los usuarios (sin filtro)
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> userIds = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        userIds.add(doc.getId());
                    }
                    Log.d(TAG, "Usuarios encontrados: " + userIds.size());
                    listener.onUsuariosCargados(userIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener usuarios: " + e.getMessage(), e);
                    listener.onError(e);
                });
    }

    /**
     * Interface para callback de usuarios
     */
    private interface OnUsuariosLoadedListener {
        void onUsuariosCargados(List<String> userIds);
        void onError(Exception e);
    }

    /**
     * Enviar notificación inmediata a TODOS los usuarios con notificaciones activadas
     */
    private void enviarNotificacionInmediata(String titulo, String mensaje, String tipo,
                                             String actividadId, String citaId) {
        // Obtener usuario actual para evitar duplicados
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String usuarioActualId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        // Obtener todos los usuarios con notificaciones activadas
        obtenerUsuariosConNotificaciones(new OnUsuariosLoadedListener() {
            @Override
            public void onUsuariosCargados(List<String> userIds) {
                if (userIds.isEmpty()) {
                    Log.w(TAG, "No hay usuarios para notificar");
                    return;
                }

                // Guardar notificación en Firestore para cada usuario
                for (String userId : userIds) {
                    guardarNotificacionParaUsuario(userId, titulo, mensaje, tipo, actividadId, citaId);
                }

                // Programar notificación local SOLO si el usuario actual está en la lista
                // Esto evita que se muestre la notificación en la barra si el usuario no tiene permisos
                if (usuarioActualId != null && userIds.contains(usuarioActualId)) {
                    programarNotificacionLocal(titulo, mensaje, tipo, actividadId, citaId);
                }

                Log.d(TAG, "✓ Notificación enviada a " + userIds.size() + " usuarios: " + titulo);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error al enviar notificación", e);
            }
        });
    }

    /**
     * Guardar notificación en Firestore para un usuario específico
     */
    private void guardarNotificacionParaUsuario(String userId, String titulo, String mensaje,
                                                String tipo, String actividadId, String citaId) {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("titulo", titulo);
        notificacion.put("mensaje", mensaje);
        notificacion.put("tipo", tipo != null ? tipo : "general");
        notificacion.put("fechaHora", System.currentTimeMillis());
        notificacion.put("leida", false);
        notificacion.put("usuarioId", userId);

        if (actividadId != null) {
            notificacion.put("actividadId", actividadId);
        }
        if (citaId != null) {
            notificacion.put("citaId", citaId);
        }

        db.collection("usuarios").document(userId)
                .collection("notificaciones")
                .add(notificacion)
                .addOnSuccessListener(ref ->
                        Log.d(TAG, "Notificación guardada para usuario: " + userId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al guardar notificación para usuario: " + userId, e));
    }

    /**
     * Programar notificación local (WorkManager) para mostrar en la barra
     */
    private void programarNotificacionLocal(String titulo, String mensaje, String tipo,
                                            String actividadId, String citaId) {
        try {
            Data inputData = new Data.Builder()
                    .putString("titulo", titulo)
                    .putString("mensaje", mensaje)
                    .putString("tipo", tipo)
                    .putString("actividadId", actividadId != null ? actividadId : "")
                    .putString("citaId", citaId != null ? citaId : "")
                    .build();

            OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(NotificacionWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(context).enqueue(work);
        } catch (Exception e) {
            Log.e(TAG, "✗ Error al programar notificación local: " + e.getMessage(), e);
        }
    }

    /**
     * Programar notificación para un momento específico
     */
    private void programarNotificacion(String titulo, String mensaje, String tipo,
                                       String actividadId, String citaId, long cuando) {
        long delay = cuando - System.currentTimeMillis();

        if (delay <= 0) {
            Log.w(TAG, "No se puede programar notificación en el pasado");
            return;
        }

        Data inputData = new Data.Builder()
                .putString("titulo", titulo)
                .putString("mensaje", mensaje)
                .putString("tipo", tipo)
                .putString("actividadId", actividadId)
                .putString("citaId", citaId)
                .build();

        String tag = citaId != null ? "cita_" + citaId : "actividad_" + actividadId;

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(NotificacionWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(tag)
                .build();

        WorkManager.getInstance(context).enqueue(work);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Log.d(TAG, "Notificación programada para: " + sdf.format(new Date(cuando)));
    }

    /**
     * Marcar notificación como leída en Firestore
     */
    public void marcarComoLeida(String notificacionId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId)
                .collection("notificaciones").document(notificacionId)
                .update("leida", true)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Notificación marcada como leída"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al marcar como leída", e));
    }

    /**
     * Eliminar notificación de Firestore
     */
    public void eliminarNotificacion(String notificacionId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId)
                .collection("notificaciones").document(notificacionId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Notificación eliminada"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar notificación", e));
    }
}