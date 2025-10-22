package com.test.mysede;

import android.os.Bundle;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.test.mysede.actividades.ListarActividadesActivity;
import com.test.mysede.citas.CancelarCitaActivity;
import com.test.mysede.citas.CrearCitaActivity;
import com.test.mysede.citas.ReagendarCitaActivity;
import com.test.mysede.oferente.OferenteActivity;
import com.test.mysede.proyecto.ProyectoActivity;
import com.test.mysede.socio.SocioComunitarioActivity;
import com.test.mysede.tipoactividad.TipoActividadActivity;
import com.test.mysede.lugar.LugarActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        MaterialButton crearCitaButton = findViewById(R.id.btn_ir_crear_cita);
        MaterialButton reagendarCitaButton = findViewById(R.id.btn_ir_reagendar_cita);
        MaterialButton cancelarCitaButton = findViewById(R.id.btn_ir_cancelar_cita);
        MaterialButton actividadesButton = findViewById(R.id.btn_ir_actividades);
        MaterialButton tipoActividadButton = findViewById(R.id.btn_ir_tipo_actividad);
        MaterialButton lugarButton = findViewById(R.id.btn_ir_lugar);
        MaterialButton btnSocio = findViewById(R.id.btn_ir_socio);
        MaterialButton btnProyectos = findViewById(R.id.btn_ir_proyectos);
        MaterialButton btnIrOferentes = findViewById(R.id.btn_ir_oferentes); // si usas botón


        crearCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, CrearCitaActivity.class))
        );

        reagendarCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, ReagendarCitaActivity.class))
        );

        cancelarCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, CancelarCitaActivity.class))
        );

        actividadesButton.setOnClickListener(v ->
                startActivity(new Intent(this, ListarActividadesActivity.class))
        );

        tipoActividadButton.setOnClickListener(v ->
                startActivity(new Intent(this, com.test.mysede.tipoactividad.TipoActividadActivity.class))
        );

        lugarButton.setOnClickListener(v ->
                startActivity(new Intent(this, LugarActivity.class))
        );

        btnSocio.setOnClickListener(v -> {
            Intent intent = new Intent(this, SocioComunitarioActivity.class);
            startActivity(intent);
        });

        btnProyectos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProyectoActivity.class);
            startActivity(intent);
        });

        btnIrOferentes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OferenteActivity.class);
            startActivity(intent);
        });

    }
}