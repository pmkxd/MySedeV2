package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import com.test.mysede.R;

public class ActivityRegister extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword, etConfirmPassword;
    private Button btnRegistrar;
    private TextView tvIrLogin;
    // private FirebaseAuth auth; // Firebase comentado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // auth = FirebaseAuth.getInstance(); // Firebase comentado

        // Bind UI
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvIrLogin = findViewById(R.id.tvIrLogin);

        // Botón registrar
        btnRegistrar.setOnClickListener(v -> registerUser());

        // Ir a Login
        tvIrLogin.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityLogin.class))
        );
    }

    private void registerUser() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Firebase comentado por problemas
        /*
        auth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                        // Guardar nombre en Firestore si después usan perfiles
                        startActivity(new Intent(this, ActivityLogin.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error al registrar", Toast.LENGTH_LONG).show();
                    }
                });
        */

        // Para pruebas de UI sin Firebase
        Toast.makeText(this, "Simulación registro exitoso", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ActivityLogin.class));
        finish();
    }
}
