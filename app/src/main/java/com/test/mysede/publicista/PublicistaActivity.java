package com.test.mysede.publicista;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.model.Usuario;
import com.test.mysede.login.ActivityLogin;

public class PublicistaActivity extends AppCompatActivity {

    private TextView txtBienvenida, txtNombreUsuario;
    private Button btnActividades, btnCalendario, btnAdjuntar;

    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicista);

        txtBienvenida = findViewById(R.id.txtBienvenida);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);

        btnActividades = findViewById(R.id.btnActividades);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnAdjuntar = findViewById(R.id.btnAdjuntar);

        usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            Toast.makeText(this, "Sesión inválida, por favor ingresa nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        // Actualizar nombre de usuario
        txtNombreUsuario.setText(usuarioActual.getNombre());
        txtBienvenida.setText("Bienvenido, " + usuarioActual.getNombre());

        // Validar permisos
        btnActividades.setEnabled(usuarioActual.tienePermiso(Permiso.VER_ACTIVIDADES));
        btnCalendario.setEnabled(usuarioActual.tienePermiso(Permiso.VER_CITAS));
        btnAdjuntar.setEnabled(usuarioActual.tienePermiso(Permiso.ADJUNTAR_ARCHIVOS));

        // Listeners
        btnActividades.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.VER_ACTIVIDADES)) {
                Toast.makeText(this, "No tienes permiso para ver actividades", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: abrir Activity de Actividades
        });

        btnCalendario.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.VER_CITAS)) {
                Toast.makeText(this, "No tienes permiso para ver el calendario", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: abrir Activity Calendario
        });

        btnAdjuntar.setOnClickListener(v -> {
            if (!usuarioActual.tienePermiso(Permiso.ADJUNTAR_ARCHIVOS)) {
                Toast.makeText(this, "No tienes permiso para adjuntar archivos", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: abrir Activity Adjuntar Archivos
        });
    }
}
