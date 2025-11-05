package com.test.mysede.mantenedores;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.test.mysede.R;
import com.test.mysede.tipoactividad.TipoActividadActivity;
import com.test.mysede.lugar.LugarActivity;
import com.test.mysede.oferente.OferenteActivity;
import com.test.mysede.socio.SocioComunitarioActivity;
import com.test.mysede.proyecto.ProyectoActivity;
import com.test.mysede.ui.SystemBarsHelper;

public class mantenedoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenedores);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Referencias a las cards
        MaterialCardView cardTipoActividad = findViewById(R.id.card_tipo_actividad);
        MaterialCardView cardLugares = findViewById(R.id.card_lugares);
        MaterialCardView cardOferentes = findViewById(R.id.card_oferentes);
        MaterialCardView cardSocios = findViewById(R.id.card_socios);
        MaterialCardView cardProyectos = findViewById(R.id.card_proyectos);

        // Clicks
        cardTipoActividad.setOnClickListener(v ->
                startActivity(new Intent(this, TipoActividadActivity.class))
        );

        cardLugares.setOnClickListener(v ->
                startActivity(new Intent(this, LugarActivity.class))
        );

        cardOferentes.setOnClickListener(v ->
                startActivity(new Intent(this, OferenteActivity.class))
        );

        cardSocios.setOnClickListener(v ->
                startActivity(new Intent(this, SocioComunitarioActivity.class))
        );

        cardProyectos.setOnClickListener(v ->
                startActivity(new Intent(this, ProyectoActivity.class))
        );
    }
}
