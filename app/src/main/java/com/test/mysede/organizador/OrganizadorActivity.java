package com.test.mysede.organizador;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Rol;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.model.Usuario;
import com.test.mysede.actividades.ListarActividadesActivity;
import com.test.mysede.CalendarActivity;
import com.test.mysede.ui.SystemBarsHelper;

public class OrganizadorActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizador);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        // Configurar Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        sessionManager = new SessionManager(this);
        usuario = sessionManager.obtenerUsuarioSesion();

        TextView txtBienvenida = findViewById(R.id.txtBienvenida);
        TextView txtNombreUsuario = findViewById(R.id.txtNombreUsuario);

        if (usuario != null) {
            txtBienvenida.setText("Bienvenido");
            txtNombreUsuario.setText(usuario.getNombre());
        }

        configurarBotones();
    }

    private void configurarBotones() {
        // Actividades
        MaterialButton btnActividades = findViewById(R.id.btnActividades);
        if (PermissionManager.tienePermiso(com.test.mysede.auth.Permiso.VER_ACTIVIDADES)) {
            btnActividades.setOnClickListener(v ->
                    startActivity(new Intent(this, ListarActividadesActivity.class))
            );
        } else {
            btnActividades.setEnabled(false);
        }

        // Calendario
        MaterialButton btnCalendario = findViewById(R.id.btnCalendario);
        if (PermissionManager.tienePermiso(com.test.mysede.auth.Permiso.VER_CALENDARIO)) {
            btnCalendario.setOnClickListener(v ->
                    startActivity(new Intent(this, CalendarActivity.class))
            );
        } else {
            btnCalendario.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cerrar_sesion) {
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        PermissionManager.setUsuarioActual(null);
        Intent intent = new Intent(this, ActivityLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        usuario = sessionManager.obtenerUsuarioSesion();
        PermissionManager.setUsuarioActual(usuario);
    }
}
