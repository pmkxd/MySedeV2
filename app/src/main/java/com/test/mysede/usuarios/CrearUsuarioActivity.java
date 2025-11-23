package com.test.mysede.usuarios;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.test.mysede.ui.SystemBarsHelper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.auth.PlantillaPermisos;
import com.test.mysede.auth.Rol;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrearUsuarioActivity extends AppCompatActivity {

    private TextInputEditText etNombreUsuario, etEmailUsuario, etPasswordUsuario, etRutUsuario;
    private TextInputLayout tilNombreUsuario, tilEmailUsuario, tilPasswordUsuario, tilRutUsuario;
    private Spinner spinnerRolUsuario;
    private LinearLayout layoutPermisos;
    private Button btnGuardarUsuario, btnCargarPlantilla;
    private MaterialCardView cardPrincipal, cardRol;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<CheckBox> checkboxesPermisos = new ArrayList<>();

    // Variables para modo edición
    private boolean modoEdicion = false;
    private Usuario usuarioAEditar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);
        SystemBarsHelper.applyEdgeToEdge(this, R.id.root_container);

        // Detectar modo edición
        String modo = getIntent().getStringExtra("modo");
        modoEdicion = "editar".equals(modo);
        if (modoEdicion) {
            usuarioAEditar = (Usuario) getIntent().getSerializableExtra("usuario");
            if (usuarioAEditar == null) {
                Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Verificar permisos
        if (!modoEdicion && !PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (modoEdicion && !PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO)) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(modoEdicion ? "Editar Usuario" : "Crear Usuario");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupRoleSpinner();
        ocultarSeccionPermisos();
        setupListeners();

        // Si es modo edición, precargar datos
        if (modoEdicion) {
            precargarDatosUsuario();
        }

        // 🔥 INICIAR ANIMACIONES DE ENTRADA
        iniciarAnimacionesEntrada();
    }

    private void initViews() {
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etEmailUsuario = findViewById(R.id.etEmailUsuario);
        etPasswordUsuario = findViewById(R.id.etPasswordUsuario);
        etRutUsuario = findViewById(R.id.etRutUsuario);

        tilNombreUsuario = findViewById(R.id.tilNombreUsuario);
        tilEmailUsuario = findViewById(R.id.tilEmailUsuario);
        tilPasswordUsuario = findViewById(R.id.tilPasswordUsuario);
        tilRutUsuario = findViewById(R.id.tilRutUsuario);

        spinnerRolUsuario = findViewById(R.id.spinnerRolUsuario);
        layoutPermisos = findViewById(R.id.layoutPermisos);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        btnCargarPlantilla = findViewById(R.id.btnCargarPlantilla);

        // Obtener las cards para animar
        cardPrincipal = (MaterialCardView) tilNombreUsuario.getParent().getParent();
        cardRol = findViewById(R.id.cardPermisos).getParent() instanceof MaterialCardView
                ? (MaterialCardView) findViewById(R.id.cardPermisos).getParent()
                : null;
    }

    private void setupRoleSpinner() {
        List<String> roles = new ArrayList<>();
        for (Rol r : Rol.values()) roles.add(r.getNombreCompleto());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRolUsuario.setAdapter(adapter);
    }

    private void ocultarSeccionPermisos() {
        if (btnCargarPlantilla != null) {
            btnCargarPlantilla.setVisibility(android.view.View.GONE);
        }
        if (layoutPermisos != null) {
            layoutPermisos.setVisibility(android.view.View.GONE);
        }
    }

    private void setupListeners() {
        btnGuardarUsuario.setOnClickListener(v -> {
            animarBotonClick(v);
            new Handler(Looper.getMainLooper()).postDelayed(this::crearUsuarioFirebase, 200);
        });

        // Animaciones al hacer focus en los campos
        setupFocusAnimations(etNombreUsuario, tilNombreUsuario);
        setupFocusAnimations(etEmailUsuario, tilEmailUsuario);
        setupFocusAnimations(etPasswordUsuario, tilPasswordUsuario);
        setupFocusAnimations(etRutUsuario, tilRutUsuario);
    }

    //ANIMACIÓN DE ENTRADA
    private void iniciarAnimacionesEntrada() {
        // Ocultar elementos inicialmente
        if (cardPrincipal != null) {
            cardPrincipal.setAlpha(0f);
            cardPrincipal.setTranslationY(100f);
            cardPrincipal.setScaleX(0.8f);
            cardPrincipal.setScaleY(0.8f);
        }

        if (cardRol != null) {
            cardRol.setAlpha(0f);
            cardRol.setTranslationY(100f);
            cardRol.setScaleX(0.8f);
            cardRol.setScaleY(0.8f);
        }

        btnGuardarUsuario.setAlpha(0f);
        btnGuardarUsuario.setTranslationY(100f);
        btnGuardarUsuario.setScaleX(0.8f);
        btnGuardarUsuario.setScaleY(0.8f);

        // Animar entrada escalonada
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animarEntradaCard(cardPrincipal, 0);
            animarEntradaCard(cardRol, 150);
            animarEntradaBoton(btnGuardarUsuario, 300);
        }, 100);
    }

    private void animarEntradaCard(View card, long delay) {
        if (card == null) return;

        card.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    private void animarEntradaBoton(View boton, long delay) {
        boton.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setStartDelay(delay)
                .setInterpolator(new AnticipateOvershootInterpolator(1.5f))
                .start();
    }

    //ANIMACIÓN DE FOCUS
    private void setupFocusAnimations(TextInputEditText editText, TextInputLayout layout) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animarFocusGanado(layout);
            } else {
                animarFocusPerdido(layout);
            }
        });
    }

    private void animarFocusGanado(View view) {
        view.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Efecto de pulso en elevación
        if (view instanceof MaterialCardView) {
            ValueAnimator elevationAnim = ValueAnimator.ofFloat(3f, 8f);
            elevationAnim.setDuration(200);
            elevationAnim.addUpdateListener(animation ->
                    ((MaterialCardView) view).setCardElevation((Float) animation.getAnimatedValue())
            );
            elevationAnim.start();
        }
    }

    private void animarFocusPerdido(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Restaurar elevación
        if (view instanceof MaterialCardView) {
            ValueAnimator elevationAnim = ValueAnimator.ofFloat(8f, 3f);
            elevationAnim.setDuration(200);
            elevationAnim.addUpdateListener(animation ->
                    ((MaterialCardView) view).setCardElevation((Float) animation.getAnimatedValue())
            );
            elevationAnim.start();
        }
    }

    //ANIMACIÓN DE CLICK EN BOTÓN
    private void animarBotonClick(View boton) {
        // Animación de squeeze (comprimir y expandir)
        AnimatorSet animSet = new AnimatorSet();

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(boton, "scaleX", 1f, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(boton, "scaleY", 1f, 0.9f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(boton, "scaleX", 0.9f, 1.05f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(boton, "scaleY", 0.9f, 1.05f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        ObjectAnimator scaleNormalX = ObjectAnimator.ofFloat(boton, "scaleX", 1.05f, 1f);
        ObjectAnimator scaleNormalY = ObjectAnimator.ofFloat(boton, "scaleY", 1.05f, 1f);
        scaleNormalX.setDuration(100);
        scaleNormalY.setDuration(100);

        // Rotación sutil
        ObjectAnimator rotation = ObjectAnimator.ofFloat(boton, "rotation", 0f, 2f, -2f, 0f);
        rotation.setDuration(300);

        animSet.play(scaleDownX).with(scaleDownY);
        animSet.play(scaleUpX).with(scaleUpY).after(scaleDownX);
        animSet.play(scaleNormalX).with(scaleNormalY).after(scaleUpX);
        animSet.play(rotation).with(scaleDownX);

        animSet.start();
    }

    //ANIMACIÓN DE SHAKE PARA ERRORES
    private void animarShakeError(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
                0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(600);
        shake.setInterpolator(new AccelerateDecelerateInterpolator());
        shake.start();

        // Cambiar color temporalmente a rojo
        if (view instanceof TextInputLayout) {
            TextInputLayout til = (TextInputLayout) view;
            til.setBoxStrokeErrorColor(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
            til.setError(" ");

            new Handler(Looper.getMainLooper()).postDelayed(() -> til.setError(null), 2000);
        }
    }

    //ANIMACIÓN DE ÉXITO
    private void animarExito(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.start();
    }

    //ANIMACIÓN DE LOADING SIMPLE
    private void animarLoadingBoton(boolean mostrar) {

        if (mostrar) {
            btnGuardarUsuario.setEnabled(false);

            // Animación simple: reducir opacidad y expandir ligeramente
            btnGuardarUsuario.animate()
                    .alpha(0.7f)
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(150)
                    .start();

            btnGuardarUsuario.setText("Guardando...");

        } else {
            btnGuardarUsuario.setEnabled(true);

            // Restaurar valores normales
            btnGuardarUsuario.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();

            btnGuardarUsuario.setRotation(0f); // por si quedó algún valor raro
            btnGuardarUsuario.setText(modoEdicion ? "Actualizar Usuario" : "Guardar Usuario");
        }
    }


    private void crearUsuarioFirebase() {
        String nombre = etNombreUsuario.getText().toString().trim();
        String email = etEmailUsuario.getText().toString().trim();
        String password = etPasswordUsuario.getText().toString().trim();
        String rut = etRutUsuario.getText().toString().trim();

        // Validaciones con animaciones
        if (nombre.isEmpty() || rut.isEmpty()) {
            if (nombre.isEmpty()) animarShakeError(tilNombreUsuario);
            if (rut.isEmpty()) animarShakeError(tilRutUsuario);
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.length() < 3) {
            animarShakeError(tilNombreUsuario);
            Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarRutChileno(rut)) {
            animarShakeError(tilRutUsuario);
            Toast.makeText(this, "El RUT ingresado no es válido (formato: 12345678-9)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Animación de éxito en campos validados
        animarExito(tilNombreUsuario);
        animarExito(tilRutUsuario);

        if (modoEdicion) {
            if (!password.isEmpty() && password.length() < 6) {
                animarShakeError(tilPasswordUsuario);
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            animarLoadingBoton(true);
            actualizarUsuarioFirebase(nombre, rut, password);
        } else {
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) animarShakeError(tilEmailUsuario);
                if (password.isEmpty()) animarShakeError(tilPasswordUsuario);
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                animarShakeError(tilEmailUsuario);
                Toast.makeText(this, "El email ingresado no es válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                animarShakeError(tilPasswordUsuario);
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            animarExito(tilEmailUsuario);
            animarExito(tilPasswordUsuario);
            animarLoadingBoton(true);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();
                        guardarUsuarioEnFirestore(uid, nombre, email, rut, password);
                    })
                    .addOnFailureListener(e -> {
                        animarLoadingBoton(false);
                        Toast.makeText(this, "Error Auth: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void guardarUsuarioEnFirestore(String uid, String nombre, String email, String rut, String pass) {
        Rol rol = Rol.fromNombreCompleto(spinnerRolUsuario.getSelectedItem().toString());
        Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);

        Usuario user = new Usuario(nombre, email, rol);
        user.setId(uid);
        user.setPermisos(permisos);

        Map<String, Object> data = new HashMap<>();
        data.put("id", uid);
        data.put("nombre", nombre);
        data.put("email", email);
        data.put("rut", rut);
        data.put("rol", rol.name());
        data.put("permisos", user.getPermisosComoLista());
        data.put("activo", true);
        data.put("fechaCreacion", user.getFechaCreacion());
        data.put("pass", pass);
        data.put("profileImageUrl", Usuario.DEFAULT_PROFILE_IMAGE_URL);
        data.put("profileImagePublicId", Usuario.DEFAULT_PROFILE_IMAGE_PUBLIC_ID);
        data.put("profileImageDeleteToken", null);

        db.collection("usuarios")
                .document(uid)
                .set(data)
                .addOnSuccessListener(a -> {
                    animarLoadingBoton(false);
                    animarExitoFinal();
                    Toast.makeText(this, "Usuario creado ✅", Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(this::finish, 500);
                })
                .addOnFailureListener(e -> {
                    animarLoadingBoton(false);
                    Toast.makeText(this, "Error Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarUsuarioFirebase(String nombre, String rut, String password) {
        if (usuarioAEditar == null) return;

        String uid = usuarioAEditar.getId();
        Rol rol = Rol.fromNombreCompleto(spinnerRolUsuario.getSelectedItem().toString());
        Set<Permiso> permisos = PlantillaPermisos.obtenerPermisosPorRol(rol);

        Usuario userTemp = new Usuario(nombre, usuarioAEditar.getEmail(), rol);
        userTemp.setPermisos(permisos);

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("rut", rut);
        updates.put("rol", rol.name());
        updates.put("permisos", userTemp.getPermisosComoLista());

        if (!password.isEmpty()) {
            updates.put("pass", password);
        }

        db.collection("usuarios")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(a -> {
                    animarLoadingBoton(false);
                    animarExitoFinal();
                    Toast.makeText(this, "Usuario actualizado ✅", Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(this::finish, 500);
                })
                .addOnFailureListener(e -> {
                    animarLoadingBoton(false);
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ANIMACIÓN FINAL DE ÉXITO RÁPIDA
    private void animarExitoFinal() {
        // Escala ligera y rápida del botón para dar sensación de éxito
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnGuardarUsuario, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnGuardarUsuario, "scaleY", 1f, 1.15f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.start();
    }


    private void precargarDatosUsuario() {
        if (usuarioAEditar == null) return;

        etNombreUsuario.setText(usuarioAEditar.getNombre());
        etEmailUsuario.setText(usuarioAEditar.getEmail());
        etEmailUsuario.setEnabled(false);
        etEmailUsuario.setFocusable(false);

        if (usuarioAEditar.getRut() != null) {
            etRutUsuario.setText(usuarioAEditar.getRut());
        }

        etPasswordUsuario.setHint("Dejar vacío para mantener contraseña actual");

        if (usuarioAEditar.getRol() != null) {
            String nombreRol = usuarioAEditar.getRol().getNombreCompleto();
            for (int i = 0; i < spinnerRolUsuario.getCount(); i++) {
                if (spinnerRolUsuario.getItemAtPosition(i).toString().equals(nombreRol)) {
                    spinnerRolUsuario.setSelection(i);
                    break;
                }
            }
        }

        btnGuardarUsuario.setText("Actualizar Usuario");
    }

    private boolean validarRutChileno(String rut) {
        if (!rut.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;
        }

        String[] partes = rut.split("-");
        String numero = partes[0];
        String dvIngresado = partes[1].toUpperCase();

        int suma = 0;
        int multiplicador = 2;

        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dvCalculado = 11 - resto;
        String dvEsperado;

        if (dvCalculado == 11) {
            dvEsperado = "0";
        } else if (dvCalculado == 10) {
            dvEsperado = "K";
        } else {
            dvEsperado = String.valueOf(dvCalculado);
        }

        return dvEsperado.equals(dvIngresado);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        // Animación de transición personalizada al salir
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}