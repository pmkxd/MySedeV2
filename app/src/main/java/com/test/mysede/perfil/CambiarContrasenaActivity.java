package com.test.mysede.perfil;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.test.mysede.R;

/**
 * Permite al usuario cambiar su contraseña actual
 * verificando primero su contraseña anterior.
 */
public class CambiarContrasenaActivity extends AppCompatActivity {

    private EditText etContrasenaActual, etNuevaContrasena, etConfirmarContrasena;
    private Button btnGuardar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        auth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbarCambiarContrasena);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
        etContrasenaActual = findViewById(R.id.etContrasenaActual);
        etNuevaContrasena = findViewById(R.id.etNuevaContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        btnGuardar = findViewById(R.id.btnGuardarContrasena);

        btnGuardar.setOnClickListener(v -> cambiarContrasena());
    }

    private void cambiarContrasena() {
        String actual = etContrasenaActual.getText().toString().trim();
        String nueva = etNuevaContrasena.getText().toString().trim();
        String confirmar = etConfirmarContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(actual) || TextUtils.isEmpty(nueva) || TextUtils.isEmpty(confirmar)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nueva.equals(confirmar)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null || usuario.getEmail() == null) {
            Toast.makeText(this, "Error al obtener el usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reautenticar antes de actualizar
        AuthCredential credencial = EmailAuthProvider.getCredential(usuario.getEmail(), actual);
        usuario.reauthenticate(credencial).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                usuario.updatePassword(nueva).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
