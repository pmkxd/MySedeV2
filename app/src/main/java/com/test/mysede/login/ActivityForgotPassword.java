package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.test.mysede.R;

public class ActivityForgotPassword extends AppCompatActivity {

    private EditText inputCorreoResetMiSede;
    private Button btnResetPasswordMiSede;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        inputCorreoResetMiSede = findViewById(R.id.inputEmailResetMiSede);
        btnResetPasswordMiSede = findViewById(R.id.btnResetPasswordMiSede);

        btnResetPasswordMiSede.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String correo = inputCorreoResetMiSede.getText().toString().trim();

        if (correo.isEmpty()) {
            inputCorreoResetMiSede.setError("Ingrese su correo");
            inputCorreoResetMiSede.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            inputCorreoResetMiSede.setError("Correo inválido");
            inputCorreoResetMiSede.requestFocus();
            return;
        }

        // Enviar correo de recuperación con Firebase
        auth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Correo enviado para restablecer contraseña",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Error enviando correo. Verifica el correo ingresado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
