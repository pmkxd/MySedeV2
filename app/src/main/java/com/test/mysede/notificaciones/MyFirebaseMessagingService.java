package com.test.mysede.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.test.mysede.R;
import com.test.mysede.perfil.PerfilActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "notificaciones_mysede";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //  Si llega una notificación desde Firebase
        if (remoteMessage.getNotification() != null) {
            String titulo = remoteMessage.getNotification().getTitle();
            String mensaje = remoteMessage.getNotification().getBody();
            mostrarNotificacion(titulo, mensaje);
        }
    }

    //  Método para mostrar la notificación local
    private void mostrarNotificacion(String titulo, String mensaje) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //  Crear canal si Android >= 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de MySede",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Canal para recibir notificaciones importantes");
            manager.createNotificationChannel(canal);
        }

        //  Abrir PerfilActivity al tocar la notificación
        Intent intent = new Intent(this, PerfilActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        //  Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
