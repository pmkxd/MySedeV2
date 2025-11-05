package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.test.mysede.R;

public class ActivityForgotPassword extends AppCompatActivity {

    private EditText inputCorreoReset;
    private Button btnResetPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        inputCorreoReset = findViewById(R.id.inputEmailResetMiSede);
        btnResetPassword = findViewById(R.id.btnResetPasswordMiSede);
        auth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String correo = inputCorreoReset.getText().toString().trim();

        if (correo.isEmpty()) {
            inputCorreoReset.setError("Ingrese su correo");
            inputCorreoReset.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            inputCorreoReset.setError("Correo inválido");
            inputCorreoReset.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo enviado para restablecer contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error enviando correo. Verifica el correo ingresado", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
