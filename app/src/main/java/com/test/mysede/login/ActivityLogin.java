package com.test.mysede.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;

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
    private TextView txtForgotPass, txtWelcomeTitle, txtSubTitle;
    private CardView logoCard;
    private MaterialCardView cardLoginContainer;

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

        // Referencias a las vistas
        logoCard = findViewById(R.id.logo_card);
        txtWelcomeTitle = findViewById(R.id.txtWelcomeTitleMiSede);
        txtSubTitle = findViewById(R.id.txtSubTitleMiSede);
        cardLoginContainer = findViewById(R.id.cardLoginContainerMiSede);
        inputCorreo = findViewById(R.id.inputCorreoMiSede);
        inputPassword = findViewById(R.id.inputPasswordMiSede);
        btnLogin = findViewById(R.id.btnLoginMiSede);
        txtForgotPass = findViewById(R.id.txtForgotPassMiSede);

        // Aplicar animaciones de entrada
        applyEnterAnimations();

        btnLogin.setOnClickListener(v -> {
            animateButtonClick(v);
            loginUser();
        });

        txtForgotPass.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityForgotPassword.class))
        );
    }

    /**
     * Aplica animaciones de entrada a todos los elementos
     */
    private void applyEnterAnimations() {
        // Hacer invisibles los elementos inicialmente
        logoCard.setAlpha(0f);
        txtWelcomeTitle.setAlpha(0f);
        txtSubTitle.setAlpha(0f);
        cardLoginContainer.setAlpha(0f);
        txtForgotPass.setAlpha(0f);

        // Animación del logo: Fade in + Scale
        logoCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(100)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.overshoot))
                .start();

        // Animación del título: Fade in + Slide up
        txtWelcomeTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(300)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();
        txtWelcomeTitle.setTranslationY(30f);

        // Animación del subtítulo: Fade in + Slide up
        txtSubTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();
        txtSubTitle.setTranslationY(30f);

        // Animación del card: Fade in + Slide up
        cardLoginContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(500)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();
        cardLoginContainer.setTranslationY(50f);

        // Animación del forgot password: Fade in
        txtForgotPass.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(700)
                .start();
    }

    /**
     * Animación de click en el botón (escala)
     */
    private void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void loginUser() {
        String correo = inputCorreo.getText().toString().trim();
        String pass = inputPassword.getText().toString().trim();

        if (correo.isEmpty()) {
            inputCorreo.setError("Ingrese su correo");
            inputCorreo.requestFocus();
            shakeView(inputCorreo);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            inputCorreo.setError("Correo inválido");
            inputCorreo.requestFocus();
            shakeView(inputCorreo);
            return;
        }

        if (pass.isEmpty()) {
            inputPassword.setError("Ingrese su contraseña");
            inputPassword.requestFocus();
            shakeView(inputPassword);
            return;
        }

        if (pass.length() < 6) {
            inputPassword.setError("La contraseña debe tener al menos 6 caracteres");
            inputPassword.requestFocus();
            shakeView(inputPassword);
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnLogin.setEnabled(false);
        btnLogin.setAlpha(0.6f);

        auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setAlpha(1f);

                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        loadUserFromFirestore(uid);
                    } else {
                        shakeView(cardLoginContainer);
                        Toast.makeText(this, "Credenciales incorrectas o usuario no encontrado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Animación de shake para errores
     */
    private void shakeView(View view) {
        view.animate()
                .translationX(-10f)
                .setDuration(50)
                .withEndAction(() -> {
                    view.animate().translationX(10f).setDuration(50)
                            .withEndAction(() -> {
                                view.animate().translationX(-10f).setDuration(50)
                                        .withEndAction(() -> {
                                            view.animate().translationX(10f).setDuration(50)
                                                    .withEndAction(() -> {
                                                        view.animate().translationX(0f).setDuration(50).start();
                                                    }).start();
                                        }).start();
                            }).start();
                })
                .start();
    }

    private void loadUserFromFirestore(String uid) {
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Usuario no registrado en Firestore", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    // Validar si el usuario está activo
                    Boolean activo = doc.getBoolean("activo");
                    if (activo == null || !activo) {
                        Toast.makeText(this,
                                "Tu cuenta ha sido deshabilitada. Contacta al administrador.",
                                Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    String nombre = doc.getString("nombre");
                    String email = doc.getString("email");
                    String rut = doc.getString("rut");
                    String rolString = doc.getString("rol");
                    List<String> permisosList = (List<String>) doc.get("permisos");
                    String avatarUrl = doc.getString("profileImageUrl");
                    String avatarPublicId = doc.getString("profileImagePublicId");
                    String avatarDeleteToken = doc.getString("profileImageDeleteToken");

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
                    usuario.setActivo(activo);

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
                    usuario.setProfileImageUrl(avatarUrl);
                    usuario.setProfileImagePublicId(avatarPublicId);
                    usuario.setProfileImageDeleteToken(avatarDeleteToken);

                    // Guardar sesión y PermissionManager
                    sessionManager.crearSesion(usuario);
                    PermissionManager.setUsuarioActual(usuario);

                    // Animación de salida antes de cambiar de pantalla
                    fadeOutAndNavigate(rol);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando datos del usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    auth.signOut();
                });
    }

    /**
     * Animación de fade out antes de navegar
     */
    private void fadeOutAndNavigate(Rol rol) {
        View rootView = findViewById(R.id.root_container);
        rootView.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    // Redirigir según rol
                    Intent intent = null;
                    switch (rol) {
                        case ADMINISTRADOR:
                            intent = new Intent(ActivityLogin.this, AdminActivity.class);
                            break;
                        case ORGANIZADOR_ACTIVIDADES:
                            intent = new Intent(ActivityLogin.this, OrganizadorActivity.class);
                            break;
                        case PROGRAMADOR_CITAS:
                            intent = new Intent(ActivityLogin.this, ProgramadorActivity.class);
                            break;
                        case PUBLICISTA:
                            intent = new Intent(ActivityLogin.this, PublicistaActivity.class);
                            break;
                    }
                    if (intent != null) {
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                    finish();
                })
                .start();
    }
}