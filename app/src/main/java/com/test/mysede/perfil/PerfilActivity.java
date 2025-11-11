package com.test.mysede.perfil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.test.mysede.R;
import com.test.mysede.notificaciones.NotificacionesActivity;

public class PerfilActivity extends AppCompatActivity {

    private SwitchMaterial switchNotificaciones, switchModoOscuro;
    private MaterialButton btnCambiarContrasena, btnVerNotificaciones;
    private TextView txtNombreUsuario;
    private ImageView imgPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Referencias UI
        imgPerfil = findViewById(R.id.imgPerfil);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        btnVerNotificaciones = findViewById(R.id.btnVerNotificaciones);

        // Nombre del usuario (ejemplo)
        txtNombreUsuario.setText("Usuario Actual");

        // Cargar preferencias guardadas
        SharedPreferences prefs = getSharedPreferences("config_prefs", MODE_PRIVATE);
        boolean notificacionesActivas = prefs.getBoolean("notificaciones", true);
        boolean modoOscuroActivo = prefs.getBoolean("modoOscuro", false);

        switchNotificaciones.setChecked(notificacionesActivas);
        switchModoOscuro.setChecked(modoOscuroActivo);

        // Cambiar preferencia de notificaciones
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notificaciones", isChecked).apply();
            String msg = isChecked ? "Notificaciones activadas" : "Notificaciones desactivadas";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Cambiar modo oscuro
        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("modoOscuro", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Botón para cambiar contraseña
        Button btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        btnCambiarContrasena.setOnClickListener(v -> {
            startActivity(new Intent(this, CambiarContrasenaActivity.class));
        });


        // Botón para ver notificaciones
        btnVerNotificaciones.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacionesActivity.class))
        );

    }
}
