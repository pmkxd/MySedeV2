package com.test.mysede.notificaciones;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificacionesAdapter adapter;
    private List<Notificacion> listaNotificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        recyclerView = findViewById(R.id.recyclerNotificaciones);
        listaNotificaciones = new ArrayList<>();

        //  Ejemplo temporal (puedes reemplazar con datos reales desde Firebase)
        listaNotificaciones.add(new Notificacion("Evento confirmado", "Tu actividad fue aprobada.", System.currentTimeMillis()));
        listaNotificaciones.add(new Notificacion("Nuevo mensaje", "Tienes una actualización en tu calendario.", System.currentTimeMillis()));

        adapter = new NotificacionesAdapter(listaNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
