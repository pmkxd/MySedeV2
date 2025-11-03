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
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.model.Usuario;
import com.test.mysede.login.ActivityLogin;

public class ProgramadorActivity extends AppCompatActivity {

    private TextView txtBienvenida, txtNombreUsuario;
    private Button btnCrearCita, btnGestionCitas, btnCalendario;

    private Usuario usuarioActual;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programador);

        // Configurar Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        sessionManager = new SessionManager(this);

        txtBienvenida = findViewById(R.id.txtBienvenida);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);

        btnCrearCita = findViewById(R.id.btnCrearCita);
        btnGestionCitas = findViewById(R.id.btnGestionCitas);
        btnCalendario = findViewById(R.id.btnCalendario);

        usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            Toast.makeText(this, "Sesión inválida, por favor ingresa nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        // Actualizar nombre de usuario en pantalla
        txtNombreUsuario.setText(usuarioActual.getNombre());
        txtBienvenida.setText("Bienvenido, " + usuarioActual.getNombre());

        // Validar permisos para cada botón
        btnCrearCita.setEnabled(usuarioActual.tienePermiso(Permiso.CREAR_CITA));
        btnGestionCitas.setEnabled(
                usuarioActual.tienePermiso(Permiso.EDITAR_CITA) ||
                        usuarioActual.tienePermiso(Permiso.VER_CITAS) ||
                        usuarioActual.tienePermiso(Permiso.ELIMINAR_CITA)
        );
        btnCalendario.setEnabled(usuarioActual.tienePermiso(Permiso.VER_CITAS));

        // Listeners
        btnCrearCita.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.CREAR_CITA)) {
                Toast.makeText(this, "No tienes permiso para crear citas", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: abrir Activity Crear Cita
        });

        btnGestionCitas.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.EDITAR_CITA) &&
                    !usuarioActual.tienePermiso(Permiso.VER_CITAS) &&
                    !usuarioActual.tienePermiso(Permiso.ELIMINAR_CITA)) {
                Toast.makeText(this, "No tienes permisos para gestionar citas", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: abrir Activity Listar/Editar Citas
        });

        btnCalendario.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.VER_CITAS)) {
                Toast.makeText(this, "No tienes permiso para ver el calendario", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: abrir Activity Calendario
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
