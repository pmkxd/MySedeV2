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
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.R;
import com.test.mysede.perfil.PerfilActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificacionWorker extends Worker {

    private static final String TAG = "NotificacionWorker";
    private static final String CHANNEL_ID = "notificaciones_mysede";

    public NotificacionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String titulo = getInputData().getString("titulo");
            String mensaje = getInputData().getString("mensaje");
            String tipo = getInputData().getString("tipo");
            String actividadId = getInputData().getString("actividadId");
            String citaId = getInputData().getString("citaId");

            if (titulo != null && mensaje != null) {
                // Guardar en Firestore
                guardarNotificacionEnFirestore(titulo, mensaje, tipo, actividadId, citaId);

                // Mostrar notificación local
                mostrarNotificacionLocal(titulo, mensaje, tipo);

                Log.d(TAG, "Notificación enviada: " + titulo);
                return Result.success();
            }

            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Error al enviar notificación", e);
            return Result.retry();
        }
    }

    private void guardarNotificacionEnFirestore(String titulo, String mensaje, String tipo,
                                                String actividadId, String citaId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Usuario no autenticado");
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
                        Log.d(TAG, "Notificación guardada en Firestore"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al guardar en Firestore", e));
    }

    private void mostrarNotificacionLocal(String titulo, String mensaje, String tipo) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de MySede",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Recordatorios de actividades y citas");
            canal.enableVibration(true);
            manager.createNotificationChannel(canal);
        }

        // Intent para abrir la app
        Intent intent = new Intent(context, NotificacionesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Determinar ícono según tipo
        int icono = obtenerIconoSegunTipo(tipo);

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icono)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private int obtenerIconoSegunTipo(String tipo) {
        if (tipo == null) return R.drawable.ic_notificacion;

        switch (tipo) {
            case "recordatorio":
                return R.drawable.ic_notificacion;
            case "confirmacion":
                return R.drawable.ic_notificacion;
            case "cambio":
                return R.drawable.ic_notificacion;
            case "cancelacion":
                return R.drawable.ic_notificacion;
            default:
                return R.drawable.ic_notificacion;
        }
    }
}