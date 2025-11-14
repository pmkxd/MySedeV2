package com.test.mysede.perfil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.model.Usuario;
import com.test.mysede.notificaciones.NotificacionesActivity;

/**
 * Activity del perfil de usuario.
 * Permite cambiar contraseña, alternar modo oscuro y gestionar notificaciones.
 */
public class PerfilActivity extends AppCompatActivity {

    private SwitchMaterial switchNotificaciones, switchModoOscuro;
    private MaterialButton btnCambiarContrasena, btnVerNotificaciones;
    private TextView txtNombreUsuario;
    private TextView txtRolUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        MaterialToolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Referencias UI
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtRolUsuario = findViewById(R.id.txtRolUsuario);
        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        btnVerNotificaciones = findViewById(R.id.btnVerNotificaciones);

        // Cargar datos del usuario desde la sesión activa
        SessionManager sessionManager = new SessionManager(this);
        Usuario usuario = PermissionManager.getUsuarioActual();
        if (usuario == null) {
            usuario = sessionManager.obtenerUsuarioSesion();
        }

        if (usuario != null) {
            txtNombreUsuario.setText(usuario.getNombre());
            txtRolUsuario.setText(usuario.getRol().getNombreCompleto());
        } else {
            txtNombreUsuario.setText(getString(R.string.perfil_nombre_desconocido));
            txtRolUsuario.setText(getString(R.string.perfil_rol_desconocido));
        }

        // Cargar preferencias persistentes
        SharedPreferences prefs = getSharedPreferences("config_prefs", MODE_PRIVATE);
        boolean notificacionesActivas = prefs.getBoolean("notificaciones", true);
        boolean modoOscuroActivo = prefs.getBoolean("modoOscuro", false);

        switchNotificaciones.setChecked(notificacionesActivas);
        switchModoOscuro.setChecked(modoOscuroActivo);

        // Listener para notificaciones
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notificaciones", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Notificaciones activadas" : "Notificaciones desactivadas",
                    Toast.LENGTH_SHORT).show();
        });

        // Listener para modo oscuro
        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("modoOscuro", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Abrir notificaciones
        btnVerNotificaciones.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacionesActivity.class))
        );
    }
}
