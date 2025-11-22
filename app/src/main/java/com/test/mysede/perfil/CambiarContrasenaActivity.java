package com.test.mysede.perfil;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.test.mysede.R;

/**
 * Permite al usuario cambiar su contraseña actual
 * verificando primero su contraseña anterior.
 * Incluye animaciones suaves y rápidas.
 */
public class CambiarContrasenaActivity extends AppCompatActivity {

    private EditText etContrasenaActual, etNuevaContrasena, etConfirmarContrasena;
    private Button btnGuardar;
    private TextInputLayout tilContrasenaActual, tilNuevaContrasena, tilConfirmarContrasena;
    private View rootLayout;

    private FirebaseAuth auth;

    private static final int ANIMATION_DURATION_SHORT = 200;
    private static final int ANIMATION_DURATION_MEDIUM = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupListeners();
        animateInitialEntry();
    }

    private void initViews() {
        rootLayout = findViewById(android.R.id.content);
        etContrasenaActual = findViewById(R.id.etContrasenaActual);
        etNuevaContrasena = findViewById(R.id.etNuevaContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        btnGuardar = findViewById(R.id.btnGuardarContrasena);

        // Obtener los TextInputLayouts si existen
        tilContrasenaActual = findViewById(R.id.tilContrasenaActual);
        tilNuevaContrasena = findViewById(R.id.tilNuevaContrasena);
        tilConfirmarContrasena = findViewById(R.id.tilConfirmarContrasena);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarCambiarContrasena);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                animateButtonPress(v);
                animateExitWithCallback();
            });
        }

        // Configurar el callback para el botón de retroceso del sistema
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                animateExitWithCallback();
            }
        });
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> {
            animateButtonPress(v);
            cambiarContrasena();
        });

        // Animación al enfocar los campos
        setupFieldAnimation(etContrasenaActual, tilContrasenaActual);
        setupFieldAnimation(etNuevaContrasena, tilNuevaContrasena);
        setupFieldAnimation(etConfirmarContrasena, tilConfirmarContrasena);
    }

    // ============================================
    // ANIMACIONES
    // ============================================

    /**
     * Animación de entrada inicial
     */
    private void animateInitialEntry() {
        rootLayout.setAlpha(0f);
        rootLayout.setTranslationY(30f);
        rootLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Animación de pulsación de botón
     */
    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    /**
     * Animación de campo de texto al enfocarse
     */
    private void setupFieldAnimation(EditText editText, TextInputLayout textInputLayout) {
        if (editText == null) return;

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            View targetView = textInputLayout != null ? textInputLayout : editText;

            if (hasFocus) {
                targetView.animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(ANIMATION_DURATION_SHORT)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            } else {
                targetView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION_SHORT)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });
    }

    /**
     * Animación de shake para errores
     */
    private void animateShake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * Animación de éxito (scale bounce)
     */
    private void animateSuccess(View view) {
        view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    /**
     * Animación de fade para deshabilitar/habilitar botón
     */
    private void animateButtonState(boolean enabled) {
        btnGuardar.setEnabled(enabled);
        btnGuardar.animate()
                .alpha(enabled ? 1f : 0.5f)
                .setDuration(ANIMATION_DURATION_SHORT)
                .start();
    }

    /**
     * Animación de salida con callback para finalizar
     */
    private void animateExitWithCallback() {
        rootLayout.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(ANIMATION_DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(this::finish)
                .start();
    }

    /**
     * Animación de salida (para uso interno sin callback)
     */
    private void animateExit() {
        rootLayout.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(ANIMATION_DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(this::finish)
                .start();
    }

    // ============================================
    // LÓGICA DE CAMBIO DE CONTRASEÑA
    // ============================================

    private void cambiarContrasena() {
        String actual = etContrasenaActual.getText().toString().trim();
        String nueva = etNuevaContrasena.getText().toString().trim();
        String confirmar = etConfirmarContrasena.getText().toString().trim();

        // Validación: campos vacíos
        if (TextUtils.isEmpty(actual) || TextUtils.isEmpty(nueva) || TextUtils.isEmpty(confirmar)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            animateShake(btnGuardar);

            // Animar campos vacíos
            if (TextUtils.isEmpty(actual)) animateShake(tilContrasenaActual != null ? tilContrasenaActual : etContrasenaActual);
            if (TextUtils.isEmpty(nueva)) animateShake(tilNuevaContrasena != null ? tilNuevaContrasena : etNuevaContrasena);
            if (TextUtils.isEmpty(confirmar)) animateShake(tilConfirmarContrasena != null ? tilConfirmarContrasena : etConfirmarContrasena);

            return;
        }

        // Validación: contraseñas no coinciden
        if (!nueva.equals(confirmar)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            animateShake(tilConfirmarContrasena != null ? tilConfirmarContrasena : etConfirmarContrasena);
            animateShake(tilNuevaContrasena != null ? tilNuevaContrasena : etNuevaContrasena);
            return;
        }

        // Validación: longitud mínima
        if (nueva.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            animateShake(tilNuevaContrasena != null ? tilNuevaContrasena : etNuevaContrasena);
            return;
        }

        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null || usuario.getEmail() == null) {
            Toast.makeText(this, "Error al obtener el usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante el proceso
        animateButtonState(false);

        // Reautenticar antes de actualizar
        AuthCredential credencial = EmailAuthProvider.getCredential(usuario.getEmail(), actual);
        usuario.reauthenticate(credencial).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Reautenticación exitosa, actualizar contraseña
                usuario.updatePassword(nueva).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        // Éxito
                        animateSuccess(btnGuardar);
                        Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show();

                        // Animar salida después de un pequeño delay
                        btnGuardar.postDelayed(this::animateExit, 800);
                    } else {
                        // Error al actualizar
                        animateButtonState(true);
                        Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                        animateShake(btnGuardar);
                    }
                });
            } else {
                // Contraseña actual incorrecta
                animateButtonState(true);
                Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                animateShake(tilContrasenaActual != null ? tilContrasenaActual : etContrasenaActual);
            }
        });
    }

    // Nota: onBackPressed() está deprecado, ahora se usa OnBackPressedCallback
    // La animación de salida se maneja en setupToolbar() con getOnBackPressedDispatcher()
}