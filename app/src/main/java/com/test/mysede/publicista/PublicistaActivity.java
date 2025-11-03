package com.test.mysede.publicista;

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
import com.test.mysede.AdjuntarArchivosActivity;
import com.test.mysede.ArchivoAdjunto;
import com.test.mysede.CalendarActivity;
import com.test.mysede.R;
import com.test.mysede.actividades.ListarActividadesActivity;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.model.Usuario;
import com.test.mysede.login.ActivityLogin;

public class PublicistaActivity extends AppCompatActivity {

    private TextView txtBienvenida, txtNombreUsuario;
    private Button btnActividades, btnCalendario, btnAdjuntar;

    private Usuario usuarioActual;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicista);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);

        txtBienvenida = findViewById(R.id.txtBienvenida);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);

        btnActividades = findViewById(R.id.btnActividades);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnAdjuntar = findViewById(R.id.btnAdjuntar);

        // Recuperar usuario
        usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            usuarioActual = sessionManager.obtenerUsuarioSesion();
            if (usuarioActual != null) {
                PermissionManager.setUsuarioActual(usuarioActual);
            }
        }

        if (usuarioActual == null) {
            Toast.makeText(this, "Sesión inválida, por favor ingresa nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        // Mostrar datos
        txtNombreUsuario.setText(usuarioActual.getNombre());
        txtBienvenida.setText("Bienvenido, " + usuarioActual.getNombre());

        // Configurar botones y habilitarlos para abrir Activities
        btnActividades.setEnabled(usuarioActual.tienePermiso(Permiso.VER_ACTIVIDADES));
        btnCalendario.setEnabled(usuarioActual.tienePermiso(Permiso.VER_CALENDARIO));
        btnAdjuntar.setEnabled(usuarioActual.tienePermiso(Permiso.ADJUNTAR_ARCHIVOS));

        // Listeners con navegación real
        btnActividades.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.VER_ACTIVIDADES)) {
                Toast.makeText(this, "No tienes permiso para ver actividades", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, ListarActividadesActivity.class));
        });

        btnCalendario.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.VER_CALENDARIO)) {
                Toast.makeText(this, "No tienes permiso para ver el calendario", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, CalendarActivity.class));
        });

        btnAdjuntar.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.ADJUNTAR_ARCHIVOS)) {
                Toast.makeText(this, "No tienes permiso para adjuntar archivos", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, AdjuntarArchivosActivity.class));
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
