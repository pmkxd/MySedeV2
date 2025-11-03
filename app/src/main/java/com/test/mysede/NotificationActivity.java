package com.test.mysede;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.test.mysede.model.Notificacion;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<Notificacion> listaNotificaciones = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        recyclerView = findViewById(R.id.recyclerNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(listaNotificaciones, notificacion -> marcarComoLeida(notificacion));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error cargando notificaciones", error);
                        Toast.makeText(this, "Error cargando notificaciones", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        Notificacion n = dc.getDocument().toObject(Notificacion.class);
                        n.setId(dc.getDocument().getId());

                        switch (dc.getType()) {
                            case ADDED:
                                listaNotificaciones.add(n);
                                break;
                            case MODIFIED:
                                int index = buscarIndex(n.getId());
                                if (index != -1) listaNotificaciones.set(index, n);
                                break;
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private int buscarIndex(String id) {
        for (int i = 0; i < listaNotificaciones.size(); i++) {
            if (listaNotificaciones.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    private void marcarComoLeida(Notificacion n) {
        if (n.isLeida()) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .document(n.getId())
                .update("leida", true)
                .addOnSuccessListener(unused -> Log.d(TAG, "Notificación marcada como leída"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar notificación", e));
    }
}
