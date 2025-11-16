package com.test.mysede.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.test.mysede.R;
import com.test.mysede.perfil.PerfilActivity;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "notificaciones_mysede";

    /**
     * Llamado cuando llega una notificación mientras la app está en primer plano
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Manejar data payload (mensajes personalizados)
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
            manejarDataPayload(remoteMessage.getData());
        }

        // Manejar notification payload
        if (remoteMessage.getNotification() != null) {
            String titulo = remoteMessage.getNotification().getTitle();
            String mensaje = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Notification payload - Título: " + titulo);

            // Extraer tipo de notificación si existe
            String tipo = remoteMessage.getData().get("tipo");
            String actividadId = remoteMessage.getData().get("actividadId");
            String citaId = remoteMessage.getData().get("citaId");

            // Guardar en Firestore
            guardarNotificacionEnFirestore(titulo, mensaje, tipo, actividadId, citaId);

            // Mostrar notificación local
            mostrarNotificacion(titulo, mensaje, tipo);
        }
    }

    /**
     * Llamado cuando se genera o actualiza el token FCM
     * IMPORTANTE: Enviar este token al servidor para poder enviar notificaciones
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);

        // Guardar token en Firestore asociado al usuario
        guardarTokenEnFirestore(token);

        // TODO: Enviar token a tu servidor backend si lo usas
        // enviarTokenAlServidor(token);
    }

    /**
     * Manejar data payload personalizado
     */
    private void manejarDataPayload(Map<String, String> data) {
        String tipo = data.get("tipo");
        String titulo = data.get("titulo");
        String mensaje = data.get("mensaje");
        String actividadId = data.get("actividadId");
        String citaId = data.get("citaId");

        if (titulo != null && mensaje != null) {
            guardarNotificacionEnFirestore(titulo, mensaje, tipo, actividadId, citaId);
            mostrarNotificacion(titulo, mensaje, tipo);
        }
    }

    /**
     * Guardar notificación recibida en Firestore
     */
    private void guardarNotificacionEnFirestore(String titulo, String mensaje, String tipo,
                                                String actividadId, String citaId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Usuario no autenticado, no se guarda notificación");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Notificación guardada con ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al guardar notificación", e));
    }

    /**
     * Guardar token FCM del dispositivo en Firestore
     */
    private void guardarTokenEnFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Usuario no autenticado, no se guarda token");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("ultimaActualizacion", System.currentTimeMillis());

        db.collection("usuarios").document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Token FCM actualizado en Firestore"))
                .addOnFailureListener(e -> {
                    // Si el documento no existe, crearlo
                    db.collection("usuarios").document(userId)
                            .set(tokenData)
                            .addOnSuccessListener(aVoid2 ->
                                    Log.d(TAG, "Token FCM guardado en nuevo documento"))
                            .addOnFailureListener(e2 ->
                                    Log.e(TAG, "Error al guardar token", e2));
                });
    }

    /**
     * Mostrar notificación local en la barra de notificaciones
     */
    private void mostrarNotificacion(String titulo, String mensaje, String tipo) {
        NotificationManager manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de MySede",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Recordatorios de actividades y citas importantes");
            canal.enableVibration(true);
            manager.createNotificationChannel(canal);
        }

        // Intent para abrir NotificacionesActivity al tocar la notificación
        Intent intent = new Intent(this, NotificacionesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Determinar ícono según el tipo
        int icono = obtenerIconoSegunTipo(tipo);

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icono)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Generar ID único para la notificación
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Notificación mostrada con ID: " + notificationId);
    }

    /**
     * Obtener ícono apropiado según el tipo de notificación
     */
    private int obtenerIconoSegunTipo(String tipo) {
        if (tipo == null) return R.drawable.ic_notificacion;

        switch (tipo) {
            case "recordatorio":
                return R.drawable.ic_notificacion; // Usar ic_alarm si existe
            case "confirmacion":
                return R.drawable.ic_notificacion; // Usar ic_check si existe
            case "cambio":
                return R.drawable.ic_notificacion; // Usar ic_edit si existe
            case "cancelacion":
                return R.drawable.ic_notificacion; // Usar ic_cancel si existe
            case "nuevo":
                return R.drawable.ic_notificacion; // Usar ic_star si existe
            default:
                return R.drawable.ic_notificacion;
        }
    }
}