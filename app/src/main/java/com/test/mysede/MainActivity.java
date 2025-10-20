package com.test.mysede;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.test.mysede.citas.CancelarCitaActivity;
import com.test.mysede.citas.CrearCitaActivity;
import com.test.mysede.citas.ReagendarCitaActivity;
import android.content.Intent;

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

        crearCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, CrearCitaActivity.class))
        );

        reagendarCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, ReagendarCitaActivity.class))
        );

        cancelarCitaButton.setOnClickListener(v ->
                startActivity(new Intent(this, CancelarCitaActivity.class))
        );
    }
}