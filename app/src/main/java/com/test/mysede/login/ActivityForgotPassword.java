package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.test.mysede.R;

// Firebase (activar después)
// import com.google.firebase.auth.FirebaseAuth;

public class ActivityForgotPassword extends AppCompatActivity {

    private EditText inputCorreoResetMiSede;
    private Button btnResetPasswordMiSede;

    // FirebaseAuth auth; // Pending enablement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        // Firebase init (descomentar cuando esté listo)
        // auth = FirebaseAuth.getInstance();

        inputCorreoResetMiSede = findViewById(R.id.inputEmailResetMiSede);
        btnResetPasswordMiSede = findViewById(R.id.btnResetPasswordMiSede);

        btnResetPasswordMiSede.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String correo = inputCorreoResetMiSede.getText().toString().trim();

        if (correo.isEmpty()) {
            Toast.makeText(this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase reset process (activar luego)
        /*
        auth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo enviado para restablecer contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error enviando correo", Toast.LENGTH_LONG).show();
                    }
                });
        */

        // Placeholder temporal para pruebas
        Toast.makeText(this,
                "Función Firebase pendiente. Validación correcta.",
                Toast.LENGTH_LONG).show();
    }
}
