package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.test.mysede.R;


public class ActivityLogin extends AppCompatActivity {

    private EditText inputCorreoMiSede, inputPasswordMiSede;
    private Button btnLoginMiSede;
    private TextView txtForgotPassMiSede;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase instance
        auth = FirebaseAuth.getInstance();

        // Bind UI
        inputCorreoMiSede = findViewById(R.id.inputCorreoMiSede);
        inputPasswordMiSede = findViewById(R.id.inputPasswordMiSede);
        btnLoginMiSede = findViewById(R.id.btnLoginMiSede);
        txtForgotPassMiSede = findViewById(R.id.txtForgotPassMiSede);

        // Login button
        btnLoginMiSede.setOnClickListener(v -> loginUser());

        // Forgot password redirect
        txtForgotPassMiSede.setOnClickListener(v ->
                Toast.makeText(this, "Pantalla de recuperación se conecta acá", Toast.LENGTH_SHORT).show()
        );
    }

    private void loginUser() {
        String correo = inputCorreoMiSede.getText().toString().trim();
        String pass = inputPasswordMiSede.getText().toString().trim();

        // Validaciones básicas UX
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

        auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Ingreso exitoso", Toast.LENGTH_SHORT).show();

                        // Redirección a Home/Dashboard del proyecto
                        // Intent intent = new Intent(ActivityLogin.this, HomeMiSedeActivity.class);
                        // startActivity(intent);
                        // finish();

                    } else {
                        Toast.makeText(this, "Credenciales incorrectas o usuario no encontrado", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
