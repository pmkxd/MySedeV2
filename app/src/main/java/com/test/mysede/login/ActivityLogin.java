package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.R;
import com.test.mysede.admin.AdminActivity;
import com.test.mysede.organizador.OrganizadorActivity;
import com.test.mysede.programador.ProgramadorActivity;
import com.test.mysede.publicista.PublicistaActivity;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.Rol;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.model.Usuario;
import com.test.mysede.ui.SystemBarsHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityLogin extends AppCompatActivity {

    private EditText inputCorreo, inputPassword;
    private Button btnLogin;
    private TextView txtForgotPass;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        inputCorreo = findViewById(R.id.inputCorreoMiSede);
        inputPassword = findViewById(R.id.inputPasswordMiSede);
        btnLogin = findViewById(R.id.btnLoginMiSede);
        txtForgotPass = findViewById(R.id.txtForgotPassMiSede);

        btnLogin.setOnClickListener(v -> loginUser());
        txtForgotPass.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityForgotPassword.class))
        );
    }

    private void loginUser() {
        String correo = inputCorreo.getText().toString().trim();
        String pass = inputPassword.getText().toString().trim();

        if (correo.isEmpty()) {
            inputCorreo.setError("Ingrese su correo");
            inputCorreo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            inputCorreo.setError("Correo inválido");
            inputCorreo.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            inputPassword.setError("Ingrese su contraseña");
            inputPassword.requestFocus();
            return;
        }

        auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        loadUserFromFirestore(uid);
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas o usuario no encontrado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadUserFromFirestore(String uid) {
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Usuario no registrado en Firestore", Toast.LENGTH_LONG).show();
                        auth.signOut(); // Cerrar sesión de Authentication
                        return;
                    }

                    // ========================================
                    // VALIDAR SI EL USUARIO ESTÁ ACTIVO
                    // ========================================
                    Boolean activo = doc.getBoolean("activo");
                    if (activo == null || !activo) {
                        Toast.makeText(this,
                                "Tu cuenta ha sido deshabilitada. Contacta al administrador.",
                                Toast.LENGTH_LONG).show();
                        auth.signOut(); // Cerrar sesión de Authentication
                        return;
                    }
                    // FIN VALIDACIÓN DE USUARIO ACTIVO

                    String nombre = doc.getString("nombre");
                    String email = doc.getString("email");
                    String rut = doc.getString("rut");
                    String rolString = doc.getString("rol");
                    List<String> permisosList = (List<String>) doc.get("permisos");

                    // Convertir rol de Firestore a enum Rol
                    Rol rol = Rol.fromNombreCompleto(rolString);
                    if (rol == null) {
                        try {
                            rol = Rol.valueOf(rolString.toUpperCase().replace(" ", "_"));
                        } catch (Exception e) {
                            Toast.makeText(this, "Rol inválido para este usuario", Toast.LENGTH_LONG).show();
                            auth.signOut();
                            return;
                        }
                    }

                    Usuario usuario = new Usuario(nombre, email, rol);
                    usuario.setRut(rut);
                    usuario.setId(uid);
                    usuario.setActivo(activo); // Guardar el estado activo

                    // Convertir permisos de String a Permiso
                    Set<Permiso> permisos = new HashSet<>();
                    if (permisosList != null) {
                        for (String p : permisosList) {
                            try {
                                permisos.add(Permiso.valueOf(p));
                            } catch (Exception ignored) {}
                        }
                    }
                    usuario.setPermisos(permisos);

                    // Guardar sesión y PermissionManager
                    sessionManager.crearSesion(usuario);
                    PermissionManager.setUsuarioActual(usuario);

                    // Redirigir según rol
                    switch (rol) {
                        case ADMINISTRADOR:
                            startActivity(new Intent(ActivityLogin.this, AdminActivity.class));
                            break;
                        case ORGANIZADOR_ACTIVIDADES:
                            startActivity(new Intent(ActivityLogin.this, OrganizadorActivity.class));
                            break;
                        case PROGRAMADOR_CITAS:
                            startActivity(new Intent(ActivityLogin.this, ProgramadorActivity.class));
                            break;
                        case PUBLICISTA:
                            startActivity(new Intent(ActivityLogin.this, PublicistaActivity.class));
                            break;
                    }
                    finish();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando datos del usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    auth.signOut();
                });
    }
}
