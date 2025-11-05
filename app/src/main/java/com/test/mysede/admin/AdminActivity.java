package com.test.mysede.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.test.mysede.R;
import com.test.mysede.actividades.ListarActividadesActivity;
import com.test.mysede.CalendarActivity;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.mantenedores.mantenedoresActivity;
import com.test.mysede.ui.SystemBarsHelper;

public class AdminActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        // Toolbar como ActionBar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);

        TextView txtBienvenida = findViewById(R.id.txtBienvenida);
        TextView txtNombreUsuario = findViewById(R.id.txtNombreUsuario);

        if (sessionManager.obtenerUsuarioSesion() != null) {
            txtBienvenida.setText("Bienvenido");
            txtNombreUsuario.setText(sessionManager.obtenerUsuarioSesion().getNombre());
        }

        configurarBotones();
    }

    private void configurarBotones() {
        // Gestion Usuarios
        Button btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnGestionUsuarios.setOnClickListener(v ->
                startActivity(new Intent(this, com.test.mysede.usuarios.GestionUsuariosActivity.class))
        );

        // Mantenedores
        Button btnMantenedores = findViewById(R.id.btnMantenedores);
        btnMantenedores.setOnClickListener(v ->
                startActivity(new Intent(this, mantenedoresActivity.class))
        );

        // Actividades
        Button btnActividades = findViewById(R.id.btnActividades);
        btnActividades.setOnClickListener(v ->
                startActivity(new Intent(this, ListarActividadesActivity.class))
        );

        // Calendario
        Button btnCalendario = findViewById(R.id.btnCalendario);
        btnCalendario.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class))
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin, menu); // solo queda cerrar sesión
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cerrar_sesion) {
            cerrarSesion();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
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
