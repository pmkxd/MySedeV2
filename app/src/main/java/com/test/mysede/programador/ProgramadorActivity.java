package com.test.mysede.programador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.test.mysede.CalendarActivity;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.citas.CrearCitaActivity;
import com.test.mysede.model.Usuario;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.ui.SystemBarsHelper;

public class ProgramadorActivity extends AppCompatActivity {

    private TextView txtBienvenida, txtNombreUsuario;
    private Button btnCrearCita, btnGestionCitas, btnCalendario;

    private Usuario usuarioActual;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programador);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);

        txtBienvenida = findViewById(R.id.txtBienvenida);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);

        btnCrearCita = findViewById(R.id.btnCrearCita);
        btnGestionCitas = findViewById(R.id.btnGestionCitas);
        btnCalendario = findViewById(R.id.btnCalendario);

        // Recuperar usuario
        usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            usuarioActual = sessionManager.obtenerUsuarioSesion();
            if (usuarioActual != null) PermissionManager.setUsuarioActual(usuarioActual);
        }

        if (usuarioActual == null) {
            Toast.makeText(this, "Sesión inválida, por favor ingresa nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        txtNombreUsuario.setText(usuarioActual.getNombre());
        txtBienvenida.setText("Bienvenido, " + usuarioActual.getNombre());

        // Habilitar siempre los botones
        btnCrearCita.setEnabled(true);
        btnGestionCitas.setEnabled(true);
        btnCalendario.setEnabled(true);

        // Listeners implementados
        btnCrearCita.setOnClickListener(v -> {
            Intent intent = new Intent(ProgramadorActivity.this, CrearCitaActivity.class);
            startActivity(intent);
        });

        btnGestionCitas.setOnClickListener(v -> {
            Intent intent = new Intent(ProgramadorActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        btnCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(ProgramadorActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

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
}
