package com.test.mysede.login;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.test.mysede.ui.SystemBarsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.test.mysede.R;

public class ActivityForgotPassword extends AppCompatActivity {

    private EditText inputCorreoReset;
    private Button btnResetPassword;
    private MaterialButton btnBackToLogin, btnGoToLogin;
    private TextView txtResetTitle, txtResetSub;
    private CardView iconContainer;
    private MaterialCardView cardResetContainer, infoCard;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        auth = FirebaseAuth.getInstance();

        // Referencias a las vistas
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        iconContainer = findViewById(R.id.icon_container);
        txtResetTitle = findViewById(R.id.txtResetTitleMiSede);
        txtResetSub = findViewById(R.id.txtResetSubMiSede);
        cardResetContainer = findViewById(R.id.cardResetContainer);
        inputCorreoReset = findViewById(R.id.inputEmailResetMiSede);
        btnResetPassword = findViewById(R.id.btnResetPasswordMiSede);
        infoCard = findViewById(R.id.info_card);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        // Configurar OnBackPressedDispatcher (método moderno)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                animateBackAndFinish();
            }
        });

        // Aplicar animaciones de entrada
        applyEnterAnimations();

        btnBackToLogin.setOnClickListener(v -> {
            animateButtonClick(v);
            animateBackAndFinish();
        });

        btnResetPassword.setOnClickListener(v -> {
            animateButtonClick(v);
            resetPassword();
        });

        btnGoToLogin.setOnClickListener(v -> {
            animateButtonClick(v);
            animateBackAndFinish();
        });
    }

    /**
     * Aplica animaciones de entrada a todos los elementos
     */
    private void applyEnterAnimations() {
        // Hacer invisibles los elementos inicialmente
        btnBackToLogin.setAlpha(0f);
        iconContainer.setAlpha(0f);
        txtResetTitle.setAlpha(0f);
        txtResetSub.setAlpha(0f);
        cardResetContainer.setAlpha(0f);
        infoCard.setAlpha(0f);
        btnGoToLogin.setAlpha(0f);

        // Animación del botón volver: Fade in + Slide from left
        btnBackToLogin.setTranslationX(-50f);
        btnBackToLogin.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(500)
                .setStartDelay(100)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();

        // Animación del icono: Fade in + Scale + Rotate
        iconContainer.setScaleX(0.5f);
        iconContainer.setScaleY(0.5f);
        iconContainer.setRotation(-180f);
        iconContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(800)
                .setStartDelay(200)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.overshoot))
                .start();

        // Animación del título: Fade in + Slide up
        txtResetTitle.setTranslationY(30f);
        txtResetTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();

        // Animación del subtítulo: Fade in + Slide up
        txtResetSub.setTranslationY(30f);
        txtResetSub.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(500)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();

        // Animación del card: Fade in + Slide up
        cardResetContainer.setTranslationY(50f);
        cardResetContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(600)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.decelerate_cubic))
                .start();

        // Animación del info card: Fade in + Scale
        infoCard.setScaleX(0.9f);
        infoCard.setScaleY(0.9f);
        infoCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(800)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.overshoot))
                .start();

        // Animación del footer: Fade in
        btnGoToLogin.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(900)
                .start();
    }

    /**
     * Animación de click en botones
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

    /**
     * Animación de pulso para el info card
     */
    private void pulseInfoCard() {
        infoCard.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .withEndAction(() -> {
                    infoCard.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void resetPassword() {
        String correo = inputCorreoReset.getText().toString().trim();

        if (correo.isEmpty()) {
            inputCorreoReset.setError("Ingrese su correo");
            inputCorreoReset.requestFocus();
            shakeView(cardResetContainer);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            inputCorreoReset.setError("Correo inválido");
            inputCorreoReset.requestFocus();
            shakeView(cardResetContainer);
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnResetPassword.setEnabled(false);
        btnResetPassword.setAlpha(0.6f);

        auth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setAlpha(1f);

                    if (task.isSuccessful()) {
                        // Animación de éxito
                        animateSuccess();
                        Toast.makeText(this, "Correo enviado para restablecer contraseña", Toast.LENGTH_LONG).show();

                        // Pulsar el info card para llamar la atención
                        pulseInfoCard();
                    } else {
                        shakeView(cardResetContainer);
                        Toast.makeText(this, "Error enviando correo. Verifica el correo ingresado", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Animación de éxito al enviar el correo
     */
    private void animateSuccess() {
        // Escalar el card hacia arriba y hacia abajo
        cardResetContainer.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction(() -> {
                    cardResetContainer.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();

        // Rotar ligeramente el icono
        iconContainer.animate()
                .rotation(360f)
                .setDuration(600)
                .setInterpolator(android.view.animation.AnimationUtils.loadInterpolator(
                        this, android.R.interpolator.overshoot))
                .start();
    }

    /**
     * Animación de fade out antes de volver
     */
    private void animateBackAndFinish() {
        View rootView = findViewById(R.id.root_container);
        rootView.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction(() -> {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .start();
    }
}