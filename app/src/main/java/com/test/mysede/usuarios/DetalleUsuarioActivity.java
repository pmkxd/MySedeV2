package com.test.mysede.usuarios;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.test.mysede.ui.SystemBarsHelper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.model.Usuario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity para ver el detalle completo de un usuario
 */
public class DetalleUsuarioActivity extends AppCompatActivity {

    private TextView tvNombre, tvEmail, tvRol, tvRut, tvEstado, tvFechaCreacion, tvUltimoAcceso;
    private LinearLayout layoutPermisos;
    private Button btnEditar;
    private Usuario usuario;

    // Variables para animaciones
    private static final int ANIMATION_DURATION = 300;
    private static final int STAGGER_DELAY = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_usuario);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de Usuario");
        }

        // Inicializar vistas
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvEmail = findViewById(R.id.tvEmailDetalle);
        tvRol = findViewById(R.id.tvRolDetalle);
        tvRut = findViewById(R.id.tvRutDetalle);
        tvEstado = findViewById(R.id.tvEstadoDetalle);
        tvFechaCreacion = findViewById(R.id.tvFechaCreacionDetalle);
        tvUltimoAcceso = findViewById(R.id.tvUltimoAccesoDetalle);
        layoutPermisos = findViewById(R.id.layoutPermisosDetalle);
        btnEditar = findViewById(R.id.btnEditarDetalle);

        // Obtener usuario del intent
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        if (usuario != null) {
            mostrarDatos();
            iniciarAnimaciones();
        } else {
            // Si no hay usuario, cerrar la activity
            finish();
        }

        // Configurar botón editar
        if (PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO)) {
            btnEditar.setOnClickListener(v -> {
                // Animación de click brutal
                animateButtonClick(v, () -> {
                    Intent intent = new Intent(this, CrearUsuarioActivity.class);
                    intent.putExtra("usuario", usuario);
                    intent.putExtra("modo", "editar");
                    startActivity(intent);
                    finish(); // Cerrar para refrescar al volver
                });
            });
        } else {
            btnEditar.setVisibility(android.view.View.GONE);
        }
    }

    private void iniciarAnimaciones() {
        // Encontrar las cards principales
        View cardInfo = findViewById(R.id.cardInfoUsuario);
        View cardPermisos = findViewById(R.id.cardPermisosUsuario);

        if (cardInfo != null) {
            animateCardEntrance(cardInfo, 0);
        }

        if (cardPermisos != null) {
            animateCardEntrance(cardPermisos, STAGGER_DELAY);
        }

        // Animar el botón con delay
        if (btnEditar != null && btnEditar.getVisibility() == View.VISIBLE) {
            animateButtonEntrance(btnEditar, STAGGER_DELAY * 2);
        }

        // Animar elementos internos después
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animateContentElements();
        }, ANIMATION_DURATION / 2);
    }

    private void animateCardEntrance(View card, int delay) {
        // Preparar vista para animación
        card.setAlpha(0f);
        card.setTranslationY(100f);
        card.setScaleX(0.9f);
        card.setScaleY(0.9f);

        // Crear animación combinada
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    private void animateButtonEntrance(View button, int delay) {
        button.setAlpha(0f);
        button.setTranslationY(50f);
        button.setScaleX(0.8f);
        button.setScaleY(0.8f);

        button.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIMATION_DURATION + 100)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    private void animateContentElements() {
        // Animar nombre con efecto reveal
        if (tvNombre != null) {
            animateTextReveal(tvNombre, 0);
        }

        // Animar email
        if (tvEmail != null) {
            animateTextSlideIn(tvEmail, STAGGER_DELAY);
        }

        // Animar rol
        if (tvRol != null) {
            animateTextSlideIn(tvRol, STAGGER_DELAY * 2);
        }

        // Animar rut si está visible
        if (tvRut != null && tvRut.getVisibility() == View.VISIBLE) {
            animateTextSlideIn(tvRut, STAGGER_DELAY * 3);
        }

        // Animar estado con efecto especial
        if (tvEstado != null) {
            animateEstadoBadge(tvEstado, STAGGER_DELAY * 4);
        }
    }

    private void animateTextReveal(TextView textView, int delay) {
        textView.setAlpha(0f);
        textView.setScaleX(0.7f);
        textView.setScaleY(0.7f);

        textView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }

    private void animateTextSlideIn(TextView textView, int delay) {
        textView.setAlpha(0f);
        textView.setTranslationX(-50f);

        textView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator(1.2f))
                .start();
    }

    private void animateEstadoBadge(TextView badge, int delay) {
        badge.setAlpha(0f);
        badge.setScaleX(0f);
        badge.setScaleY(0f);

        badge.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIMATION_DURATION + 50)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(2.0f))
                .start();
    }

    private void animateButtonClick(View button, Runnable onComplete) {
        // Efecto de presión brutal
        AnimatorSet pressSet = new AnimatorSet();

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.92f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.92f);

        pressSet.playTogether(scaleDownX, scaleDownY);
        pressSet.setDuration(100);
        pressSet.setInterpolator(new AccelerateDecelerateInterpolator());

        pressSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Bounce back
                AnimatorSet releaseSet = new AnimatorSet();
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.92f, 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.92f, 1f);

                releaseSet.playTogether(scaleUpX, scaleUpY);
                releaseSet.setDuration(150);
                releaseSet.setInterpolator(new OvershootInterpolator(2f));

                releaseSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });

                releaseSet.start();
            }
        });

        pressSet.start();
    }

    private void mostrarDatos() {
        if (usuario == null) return;

        tvNombre.setText(usuario.getNombre());
        tvEmail.setText(usuario.getEmail());
        tvRol.setText(usuario.getRol() != null ? usuario.getRol().getNombreCompleto() : "Sin rol");

        // RUT (si existe)
        if (tvRut != null) {
            if (usuario.getRut() != null && !usuario.getRut().isEmpty()) {
                tvRut.setText(usuario.getRut());
                tvRut.setVisibility(android.view.View.VISIBLE);
            } else {
                tvRut.setVisibility(android.view.View.GONE);
            }
        }

        // Estado
        if (usuario.isActivo()) {
            tvEstado.setText("Activo");
            tvEstado.setTextColor(getColor(R.color.md_theme_primary));
        } else {
            tvEstado.setText("Inactivo");
            tvEstado.setTextColor(getColor(R.color.md_theme_error));
        }

        // Formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Fecha de creación
        String fecha = sdf.format(new Date(usuario.getFechaCreacion()));
        tvFechaCreacion.setText("Creado: " + fecha);

        // Último acceso (si existe el TextView)
        if (tvUltimoAcceso != null) {
            String ultimoAcceso = sdf.format(new Date(usuario.getUltimoAcceso()));
            tvUltimoAcceso.setText("Último acceso: " + ultimoAcceso);
        }

        // Mostrar permisos agrupados por categoría
        layoutPermisos.removeAllViews();

        if (usuario.getPermisos() != null && !usuario.getPermisos().isEmpty()) {
            int itemIndex = 0;

            for (Permiso.Categoria categoria : Permiso.Categoria.values()) {
                boolean tienePermisosDeCategoria = false;

                // Verificar si tiene permisos de esta categoría
                for (Permiso permiso : usuario.getPermisos()) {
                    if (permiso.getCategoria() == categoria) {
                        tienePermisosDeCategoria = true;
                        break;
                    }
                }

                if (tienePermisosDeCategoria) {
                    // Header de categoría
                    TextView tvCategoria = new TextView(this);
                    tvCategoria.setText(categoria.getNombre());
                    tvCategoria.setTextSize(16);
                    tvCategoria.setTextColor(getColor(R.color.md_theme_primary));
                    tvCategoria.setTextAppearance(android.R.style.TextAppearance_Material_Subhead);
                    LinearLayout.LayoutParams paramsCategoria = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    paramsCategoria.setMargins(0, 24, 0, 8);
                    tvCategoria.setLayoutParams(paramsCategoria);

                    // Preparar para animación
                    tvCategoria.setAlpha(0f);
                    tvCategoria.setTranslationX(-30f);

                    layoutPermisos.addView(tvCategoria);

                    // Animar categoría con delay
                    final int categoryDelay = ANIMATION_DURATION + (itemIndex * 40);
                    tvCategoria.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(250)
                            .setStartDelay(categoryDelay)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();

                    itemIndex++;

                    // Permisos de esta categoría
                    for (Permiso permiso : usuario.getPermisos()) {
                        if (permiso.getCategoria() == categoria) {
                            TextView tvPermiso = new TextView(this);
                            tvPermiso.setText("• " + permiso.getDescripcion());
                            tvPermiso.setTextSize(14);
                            tvPermiso.setTextColor(getColor(R.color.md_theme_onSurface));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(16, 4, 0, 4);
                            tvPermiso.setLayoutParams(params);

                            // Preparar para animación
                            tvPermiso.setAlpha(0f);
                            tvPermiso.setTranslationX(-20f);

                            layoutPermisos.addView(tvPermiso);

                            // Animar permiso con efecto cascada
                            final int permisoDelay = ANIMATION_DURATION + (itemIndex * 40);
                            tvPermiso.animate()
                                    .alpha(1f)
                                    .translationX(0f)
                                    .setDuration(250)
                                    .setStartDelay(permisoDelay)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();

                            itemIndex++;
                        }
                    }
                }
            }
        } else {
            // Sin permisos
            TextView tvSinPermisos = new TextView(this);
            tvSinPermisos.setText("Este usuario no tiene permisos asignados");
            tvSinPermisos.setTextSize(14);
            tvSinPermisos.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));

            // Preparar para animación
            tvSinPermisos.setAlpha(0f);
            tvSinPermisos.setTranslationY(20f);

            layoutPermisos.addView(tvSinPermisos);

            // Animar mensaje
            tvSinPermisos.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}