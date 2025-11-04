package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import com.test.mysede.R;

public class ActivityLogin extends AppCompatActivity {

    private EditText inputCorreoMiSede, inputPasswordMiSede;
    private Button btnLoginMiSede;
    private TextView txtForgotPassMiSede;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputCorreoMiSede = findViewById(R.id.inputCorreoMiSede);
        inputPasswordMiSede = findViewById(R.id.inputPasswordMiSede);
        btnLoginMiSede = findViewById(R.id.btnLoginMiSede);
        txtForgotPassMiSede = findViewById(R.id.txtForgotPassMiSede);

        btnLoginMiSede.setOnClickListener(v -> loginUser());

        txtForgotPassMiSede.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityForgotPassword.class));
        });
    }

    private void loginUser() {
        String correo = inputCorreoMiSede.getText().toString().trim();
        String pass = inputPasswordMiSede.getText().toString().trim();

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
                        String uid = auth.getCurrentUser().getUid();
                        validateUserProfile(uid, correo);
                    } else {
                        Toast.makeText(this,
                                "Credenciales incorrectas o usuario no encontrado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void validateUserProfile(String uid, String correo) {
        DocumentReference userRef = db.collection("usuarios").document(uid);

        userRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                // Crear perfil mínimo si no existe
                userRef.set(new UserProfile(correo, "usuario", "sin_sede"))
                        .addOnSuccessListener(aVoid ->
                                goToHome("Login OK + perfil creado"))
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error creando perfil", Toast.LENGTH_SHORT).show());
            } else {
                goToHome("Login OK");
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error verificando perfil", Toast.LENGTH_SHORT).show());
    }

    private void goToHome(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // startActivity(new Intent(ActivityLogin.this, HomeMiSedeActivity.class));
        // finish();
    }

    public static class UserProfile {
        public String correo;
        public String rol;
        public String sede;

        public UserProfile() {}

        public UserProfile(String correo, String rol, String sede) {
            this.correo = correo;
            this.rol = rol;
            this.sede = sede;
        }
    }
}
