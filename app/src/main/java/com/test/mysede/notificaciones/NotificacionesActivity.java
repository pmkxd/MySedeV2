package com.test.mysede.notificaciones;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.test.mysede.R;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private static final String TAG = "NotificacionesActivity";

    private RecyclerView recyclerView;
    private NotificacionesAdapter adapter;
    private List<Notificacion> listaNotificaciones;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutSinNotificaciones;
    private TextView tvSinNotificaciones;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Notificaciones");
            }
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerNotificaciones);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutSinNotificaciones = findViewById(R.id.layoutSinNotificaciones);
        tvSinNotificaciones = findViewById(R.id.tvSinNotificaciones);

        listaNotificaciones = new ArrayList<>();

        // Configurar RecyclerView
        adapter = new NotificacionesAdapter(listaNotificaciones, this::onNotificacionClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar SwipeRefresh
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                cargarNotificaciones();
                swipeRefresh.setRefreshing(false);
            });
        }

        // Cargar notificaciones
        cargarNotificaciones();
    }

    /**
     * Cargar notificaciones desde Firestore
     */
    private void cargarNotificaciones() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId)
                .collection("notificaciones")
                .orderBy("fechaHora", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaNotificaciones.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notificacion notif = doc.toObject(Notificacion.class);
                        if (notif != null) {
                            notif.setId(doc.getId());
                            listaNotificaciones.add(notif);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    actualizarVista();

                    Log.d(TAG, "Notificaciones cargadas: " + listaNotificaciones.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar notificaciones", e);
                    Toast.makeText(this, "Error al cargar notificaciones", Toast.LENGTH_SHORT).show();
                    actualizarVista();
                });
    }

    /**
     * Actualizar vista según si hay o no notificaciones
     */
    private void actualizarVista() {
        if (listaNotificaciones.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (layoutSinNotificaciones != null) {
                layoutSinNotificaciones.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (layoutSinNotificaciones != null) {
                layoutSinNotificaciones.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Manejar clic en una notificación
     */
    private void onNotificacionClick(Notificacion notificacion) {
        // Marcar como leída
        GestorNotificaciones gestor = new GestorNotificaciones(this);
        gestor.marcarComoLeida(notificacion.getId());

        // Actualizar visualmente
        notificacion.setLeida(true);
        adapter.notifyDataSetChanged();

        // TODO: Abrir la actividad relacionada si existe actividadId
        if (notificacion.getActividadId() != null) {
            // Intent intent = new Intent(this, DetalleActividadActivity.class);
            // intent.putExtra("actividadId", notificacion.getActividadId());
            // startActivity(intent);

            Toast.makeText(this, "Abrir actividad: " + notificacion.getActividadId(),
                    Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Notificación clickeada: " + notificacion.getTitulo());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar notificaciones al volver a la actividad
        cargarNotificaciones();
    }
}