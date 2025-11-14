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
import com.test.mysede.login.ActivityLogin;

/**
 * Activity del perfil de usuario.
 * Permite cambiar contraseña, alternar modo oscuro y gestionar notificaciones.
 */
public class PerfilActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "config_prefs";
    private static final String PREF_NOTIFICACIONES = "notificaciones";
    private static final String PREF_MODO_OSCURO = "modoOscuro";

    private SwitchMaterial switchNotificaciones, switchModoOscuro;
    private MaterialButton btnCambiarContrasena;
    private MaterialButton btnVerNotificaciones, btnCerrarSesion;
    private TextView txtNombreUsuario;
    private TextView txtRolUsuario;
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
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
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        // Cargar datos del usuario desde la sesión activa
        sessionManager = new SessionManager(this);
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
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        configurarSwitchNotificaciones();
        configurarSwitchModoOscuro();

        // Abrir notificaciones
        btnVerNotificaciones.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacionesActivity.class))
        );
        // Abrir cambiar contraseña
        btnCambiarContrasena.setOnClickListener(v ->
                startActivity(new Intent(this, CambiarContrasenaActivity.class))
        );

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void configurarSwitchNotificaciones() {
        boolean notificacionesActivas = sharedPreferences.getBoolean(PREF_NOTIFICACIONES, true);
        switchNotificaciones.setChecked(notificacionesActivas);
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(PREF_NOTIFICACIONES, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? getString(R.string.perfil_notificaciones_activadas)
                            : getString(R.string.perfil_notificaciones_desactivadas),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarSwitchModoOscuro() {
        boolean modoOscuroActivo = sharedPreferences.getBoolean(PREF_MODO_OSCURO, false);
        switchModoOscuro.setChecked(modoOscuroActivo);
        aplicarModoOscuro(modoOscuroActivo);
        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(PREF_MODO_OSCURO, isChecked).apply();
            aplicarModoOscuro(isChecked);
        });
}
    private void aplicarModoOscuro(boolean activar) {
        int modoDeseado = activar ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != modoDeseado) {
            AppCompatDelegate.setDefaultNightMode(modoDeseado);
            recreate();
        }
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        PermissionManager.setUsuarioActual(null);
        Intent intent = new Intent(this, ActivityLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
