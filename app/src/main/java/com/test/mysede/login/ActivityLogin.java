package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.test.mysede.R;

// Firebase (comentado hasta validación final)
// import com.google.firebase.auth.FirebaseAuth;

public class ActivityLogin extends AppCompatActivity {

    private EditText inputCorreoMiSede, inputPasswordMiSede;
    private Button btnLoginMiSede;
    private TextView txtForgotPassMiSede;

    // FirebaseAuth auth; // Pending enablement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase init (activar cuando esté configurado)
        // auth = FirebaseAuth.getInstance();

        // Bind UI
        inputCorreoMiSede = findViewById(R.id.inputCorreoMiSede);
        inputPasswordMiSede = findViewById(R.id.inputPasswordMiSede);
        btnLoginMiSede = findViewById(R.id.btnLoginMiSede);
        txtForgotPassMiSede = findViewById(R.id.txtForgotPassMiSede);

        // Login button
        btnLoginMiSede.setOnClickListener(v -> loginUser());

        // Forgot password
        txtForgotPassMiSede.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Aquí va la navegación hacia recuperar contraseña",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String correo = inputCorreoMiSede.getText().toString().trim();
        String pass = inputPasswordMiSede.getText().toString().trim();

        // Validación UX
        if (correo.isEmpty()) {
            inputCorreoMiSede.setError("Ingrese su correo");
            inputCorreoMiSede.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            inputPasswordMiSede.setError("Ingrese su contraseña");
            inputPasswordMiSede.requestFocus();
            return;
        }

        // Firebase login block (activarlo luego)
        /*
        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Ingreso exitoso", Toast.LENGTH_SHORT).show();

                    // Intent intent = new Intent(ActivityLogin.this, HomeMiSedeActivity.class);
                    // startActivity(intent);
                    // finish();
                } else {
                    Toast.makeText(this,
                            "Credenciales incorrectas o usuario no encontrado",
                            Toast.LENGTH_LONG).show();
                }
            });
        */

        // Placeholder temporal para test de UI
        Toast.makeText(this,
                "Firebase pendiente. Validación local OK.",
                Toast.LENGTH_SHORT).show();
    }
}
