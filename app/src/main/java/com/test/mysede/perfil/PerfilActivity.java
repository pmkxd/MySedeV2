package com.test.mysede.perfil;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.test.mysede.R;
import com.test.mysede.DAO.PerfilImagenDAO;
import com.test.mysede.DAO.UsuariosDAO;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.BuildConfig;
import com.test.mysede.model.PerfilImagenResultado;
import com.test.mysede.model.Usuario;
import com.test.mysede.notificaciones.NotificacionesActivity;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

/**
 * Activity del perfil de usuario con animaciones premium.
 * Permite cambiar contraseña, alternar modo oscuro y gestionar notificaciones.
 */
public class PerfilActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "config_prefs";
    private static final String PREF_NOTIFICACIONES = "notificaciones";
    private static final String PREF_MODO_OSCURO = "modoOscuro";

    private SwitchMaterial switchNotificaciones;
    private SwitchMaterial switchModoOscuro;
    private MaterialButton btnCambiarContrasena;
    private MaterialButton btnVerNotificaciones;
    private MaterialButton btnCerrarSesion;
    private MaterialButton btnEditarFotoPerfil;
    private ShapeableImageView imgPerfil;
    private CircularProgressIndicator progressAvatar;
    private TextView txtNombreUsuario;
    private TextView txtRolUsuario;
    private View profileCard;
    private View switchesCard;
    private View buttonsContainer;

    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private Usuario usuario;
    private PerfilImagenDAO perfilImagenDAO;
    private UsuariosDAO usuariosDAO;
    private ActivityResultLauncher<String> seleccionarImagenLauncher;
    private ActivityResultLauncher<Intent> recortarImagenLauncher;
    private ActivityResultLauncher<Uri> tomarFotoLauncher;
    private ActivityResultLauncher<String> permisoCamaraLauncher;
    @Nullable
    private Uri uriImagenOriginal;
    @Nullable
    private Uri uriFotoTemporal;
    @Nullable
    private File archivoFotoTemporal;
    private boolean imagenDesdeCamara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        MaterialToolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            animarBoton(v);
            getOnBackPressedDispatcher().onBackPressed();
        });

        // Inicializar vistas
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtRolUsuario = findViewById(R.id.txtRolUsuario);
        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        btnVerNotificaciones = findViewById(R.id.btnVerNotificaciones);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnEditarFotoPerfil = findViewById(R.id.btnEditarFotoPerfil);
        imgPerfil = findViewById(R.id.imgPerfil);
        progressAvatar = findViewById(R.id.progressAvatar);

        // Buscar contenedores para animar (con casts seguros)
        try {
            profileCard = (View) txtNombreUsuario.getParent().getParent();
        } catch (ClassCastException e) {
            profileCard = null;
        }

        try {
            switchesCard = (View) switchNotificaciones.getParent().getParent().getParent();
        } catch (ClassCastException e) {
            switchesCard = null;
        }

        try {
            buttonsContainer = (View) btnCambiarContrasena.getParent();
        } catch (ClassCastException e) {
            buttonsContainer = null;
        }

        sessionManager = new SessionManager(this);
        usuario = PermissionManager.getUsuarioActual();
        if (usuario == null) {
            usuario = sessionManager.obtenerUsuarioSesion();
        }

        perfilImagenDAO = new PerfilImagenDAO();
        usuariosDAO = new UsuariosDAO();
        configurarLaunchers();

        if (usuario != null) {
            txtNombreUsuario.setText(usuario.getNombre());
            txtRolUsuario.setText(usuario.getRol().getNombreCompleto());
            ProfileImageLoader.loadIntoImageView(imgPerfil, usuario.getProfileImageUrl());
        } else {
            txtNombreUsuario.setText(getString(R.string.perfil_nombre_desconocido));
            txtRolUsuario.setText(getString(R.string.perfil_rol_desconocido));
            ProfileImageLoader.loadIntoImageView(imgPerfil, null);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        configurarSwitchNotificaciones();
        configurarSwitchModoOscuro();

        // Configurar listeners con animaciones
        btnVerNotificaciones.setOnClickListener(v -> {
            animarBoton(v);
            startActivity(new Intent(this, NotificacionesActivity.class));
        });

        btnCambiarContrasena.setOnClickListener(v -> {
            animarBoton(v);
            startActivity(new Intent(this, CambiarContrasenaActivity.class));
        });

        btnEditarFotoPerfil.setOnClickListener(v -> {
            animarBoton(v);
            mostrarOpcionesImagen();
        });

        btnCerrarSesion.setOnClickListener(v -> {
            animarBoton(v);
            cerrarSesion();
        });

        // Animación de entrada
        animarEntrada();
    }

    /**
     * Animación de entrada para todos los elementos
     */
    private void animarEntrada() {
        // Animar toolbar (fade in)
        View toolbar = findViewById(R.id.toolbarPerfil);
        if (toolbar != null) {
            toolbar.setAlpha(0f);
            toolbar.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Animar card del perfil (scale + fade)
        if (profileCard != null) {
            profileCard.setAlpha(0f);
            profileCard.setScaleX(0.9f);
            profileCard.setScaleY(0.9f);
            profileCard.setTranslationY(20f);
            profileCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(100)
                    .setInterpolator(new OvershootInterpolator(0.8f))
                    .start();
        }

        // Animar switches card
        if (switchesCard != null) {
            switchesCard.setAlpha(0f);
            switchesCard.setTranslationX(-30f);
            switchesCard.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(350)
                    .setStartDelay(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Animar botones uno por uno
        if (buttonsContainer != null) {
            animarBotonEntrada(btnCambiarContrasena, 0);
            animarBotonEntrada(btnVerNotificaciones, 50);
            animarBotonEntrada(btnCerrarSesion, 100);
        }
    }

    /**
     * Anima la entrada de un botón individual
     */
    private void animarBotonEntrada(View boton, int delayExtra) {
        if (boton != null) {
            boton.setAlpha(0f);
            boton.setTranslationX(40f);
            boton.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .setStartDelay(400 + delayExtra)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * Animación de pulsación para botones
     */
    private void animarBoton(View boton) {
        boton.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(80)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    boton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
    }

    /**
     * Animación para el avatar cuando rota
     */
    private void animarRotacionAvatar() {
        imgPerfil.animate()
                .rotationBy(360f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void configurarLaunchers() {
        permisoCamaraLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                iniciarCapturaFoto();
            } else {
                Toast.makeText(this, R.string.perfil_error_permiso_camara, Toast.LENGTH_SHORT).show();
            }
        });

        tomarFotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (Boolean.TRUE.equals(result)) {
                if (uriFotoTemporal != null) {
                    uriImagenOriginal = uriFotoTemporal;
                    iniciarRecorte(uriFotoTemporal);
                }
            } else {
                eliminarTemporal(uriFotoTemporal);
                limpiarCapturaCamara();
            }
        });

        seleccionarImagenLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imagenDesdeCamara = false;
                uriImagenOriginal = uri;
                iniciarRecorte(uri);
            }
        });

        recortarImagenLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uriResultado = UCrop.getOutput(result.getData());
                if (uriResultado != null) {
                    mostrarPreviewAvatar(uriResultado);
                } else {
                    Toast.makeText(this, R.string.perfil_error_procesar_imagen, Toast.LENGTH_SHORT).show();
                    if (imagenDesdeCamara) {
                        limpiarCapturaCamara();
                    }
                }
            } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                Throwable error = UCrop.getError(result.getData());
                String mensaje = error != null ? error.getMessage() : getString(R.string.perfil_error_procesar_imagen);
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                limpiarCapturaCamara();
            }
        });
    }

    private void iniciarSeleccionImagen() {
        seleccionarImagenLauncher.launch("image/*");
    }

    private void mostrarOpcionesImagen() {
        String[] opciones = {
                getString(R.string.perfil_imagen_opcion_tomar_foto),
                getString(R.string.perfil_imagen_opcion_galeria)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.perfil_imagen_opciones_titulo)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        verificarPermisoCamara();
                    } else {
                        iniciarSeleccionImagen();
                    }
                })
                .show();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCapturaFoto();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarCapturaFoto() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, R.string.perfil_error_camara_no_disponible, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (directorio == null) {
                directorio = getCacheDir();
            }
            archivoFotoTemporal = File.createTempFile("avatar_captura_", ".jpg", directorio);
            uriFotoTemporal = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", archivoFotoTemporal);
            imagenDesdeCamara = true;
            tomarFotoLauncher.launch(uriFotoTemporal);
        } catch (IOException e) {
            Toast.makeText(this, R.string.perfil_error_generar_archivo, Toast.LENGTH_SHORT).show();
            limpiarCapturaCamara();
        }
    }

    private void iniciarRecorte(@NonNull Uri origen) {
        uriImagenOriginal = origen;
        Uri destino = Uri.fromFile(new File(getCacheDir(), "avatar_cortado_" + System.currentTimeMillis() + ".jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(true);
        options.setCircleDimmedLayer(true);
        options.setFreeStyleCropEnabled(false);
        Intent intent = UCrop.of(origen, destino)
                .withAspectRatio(1, 1)
                .withMaxResultSize(2048, 2048)
                .withOptions(options)
                .getIntent(this);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        recortarImagenLauncher.launch(intent);
    }

    private void mostrarPreviewAvatar(@NonNull Uri uriRecortada) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_preview_avatar, null, false);
        ImageView imagenPreview = vista.findViewById(R.id.imgPreviewAvatar);
        imagenPreview.setImageURI(uriRecortada);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.perfil_preview_titulo)
                .setView(vista)
                .setPositiveButton(R.string.perfil_preview_confirmar, null)
                .setNegativeButton(R.string.perfil_preview_cancelar, (dialog, which) -> {
                    eliminarTemporal(uriRecortada);
                    uriImagenOriginal = null;
                    if (imagenDesdeCamara) {
                        limpiarCapturaCamara();
                    }
                })
                .setNeutralButton(R.string.perfil_preview_reajustar, null)
                .create();
        dialogo.setCanceledOnTouchOutside(false);
        dialogo.setCancelable(false);
        dialogo.setOnShowListener(d -> {
            Button botonConfirmar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
            Button botonReajustar = dialogo.getButton(AlertDialog.BUTTON_NEUTRAL);
            botonConfirmar.setOnClickListener(v -> {
                dialogo.dismiss();
                subirNuevaImagen(uriRecortada);
            });
            botonReajustar.setOnClickListener(v -> {
                dialogo.dismiss();
                eliminarTemporal(uriRecortada);
                if (uriImagenOriginal != null) {
                    iniciarRecorte(uriImagenOriginal);
                } else {
                    Toast.makeText(this, R.string.perfil_error_procesar_imagen, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Animar entrada del diálogo
        vista.setScaleX(0.8f);
        vista.setScaleY(0.8f);
        vista.setAlpha(0f);
        vista.post(() -> {
            vista.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(250)
                    .setInterpolator(new OvershootInterpolator(1f))
                    .start();
        });

        dialogo.show();
    }

    private void subirNuevaImagen(@NonNull Uri uriRecortada) {
        if (usuario == null) {
            Toast.makeText(this, R.string.perfil_error_usuario_no_disponible, Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarCargaImagen(true);
        btnEditarFotoPerfil.setEnabled(false);
        String tokenAnterior = usuario.getProfileImageDeleteToken();
        Task<PerfilImagenResultado> tareaSubida = perfilImagenDAO.subirAvatar(this, uriRecortada);
        tareaSubida
                .addOnSuccessListener(resultado -> {
                    if (resultado == null || TextUtils.isEmpty(resultado.getUrl())) {
                        Toast.makeText(this, R.string.perfil_error_subir_imagen, Toast.LENGTH_SHORT).show();
                        finalizarActualizacion(uriRecortada);
                        return;
                    }
                    usuario.setProfileImageUrl(resultado.getUrl());
                    usuario.setProfileImagePublicId(resultado.getPublicId());
                    usuario.setProfileImageDeleteToken(resultado.getDeleteToken());
                    usuariosDAO.actualizarImagenPerfil(usuario)
                            .addOnSuccessListener(unused -> {
                                sessionManager.crearSesion(usuario);
                                PermissionManager.setUsuarioActual(usuario);
                                ProfileImageLoader.loadIntoImageView(imgPerfil, usuario.getProfileImageUrl());

                                // Animar actualización exitosa
                                animarRotacionAvatar();

                                Toast.makeText(this, R.string.perfil_imagen_actualizada, Toast.LENGTH_SHORT).show();
                                if (!TextUtils.isEmpty(tokenAnterior)) {
                                    perfilImagenDAO.eliminarAvatarPorToken(tokenAnterior);
                                }
                            })
                            .addOnFailureListener(e -> {
                                String mensaje = getString(
                                        R.string.perfil_error_subir_imagen_detalle,
                                        e != null ? e.getMessage() : "Error desconocido"
                                );
                                new MaterialAlertDialogBuilder(this)
                                        .setTitle("Error al subir imagen")
                                        .setMessage(mensaje)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                                        .show();
                                finalizarActualizacion(uriRecortada);
                            })
                            .addOnCompleteListener(task -> finalizarActualizacion(uriRecortada));
                })
                .addOnFailureListener(e -> {
                    String mensaje = getString(
                            R.string.perfil_error_subir_imagen_detalle,
                            e != null ? e.getMessage() : "Error desconocido"
                    );
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Error al subir imagen")
                            .setMessage(mensaje)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .show();
                    finalizarActualizacion(uriRecortada);
                })
                .addOnCompleteListener(task -> {
                    eliminarTemporal(uriRecortada);
                    limpiarCapturaCamara();
                });
    }

    private void finalizarActualizacion(@NonNull Uri uriRecortada) {
        mostrarCargaImagen(false);
        btnEditarFotoPerfil.setEnabled(true);
    }

    private void restaurarAvatar() {
        if (usuario != null) {
            ProfileImageLoader.loadIntoImageView(imgPerfil, usuario.getProfileImageUrl());
        }
    }

    private void eliminarTemporal(@Nullable Uri uri) {
        if (uri != null && "file".equals(uri.getScheme())) {
            File archivo = new File(uri.getPath());
            if (archivo.exists()) {
                //noinspection ResultOfMethodCallIgnored
                archivo.delete();
            }
        }
    }

    private void limpiarCapturaCamara() {
        if (archivoFotoTemporal != null && archivoFotoTemporal.exists()) {
            //noinspection ResultOfMethodCallIgnored
            archivoFotoTemporal.delete();
        }
        archivoFotoTemporal = null;
        uriFotoTemporal = null;
        imagenDesdeCamara = false;
        uriImagenOriginal = null;
    }

    private void mostrarCargaImagen(boolean mostrar) {
        progressAvatar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        progressAvatar.setIndeterminate(mostrar);

        // Animación del avatar durante la carga
        if (mostrar) {
            imgPerfil.animate()
                    .alpha(0.5f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            imgPerfil.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void configurarSwitchNotificaciones() {
        boolean notificacionesActivas = sharedPreferences.getBoolean(PREF_NOTIFICACIONES, true);
        switchNotificaciones.setChecked(notificacionesActivas);
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Animación del switch
            animarSwitch(buttonView);

            sharedPreferences.edit().putBoolean(PREF_NOTIFICACIONES, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? getString(R.string.perfil_notificaciones_activadas)
                            : getString(R.string.perfil_notificaciones_desactivadas),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarSwitchModoOscuro() {
        boolean modoOscuroActivo = sharedPreferences.getBoolean(PREF_MODO_OSCURO, false);
        switchModoOscuro.setChecked(modoOscuroActivo);
        aplicarModoOscuro(modoOscuroActivo);
        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Animación del switch
            animarSwitch(buttonView);

            sharedPreferences.edit().putBoolean(PREF_MODO_OSCURO, isChecked).apply();
            aplicarModoOscuro(isChecked);
        });
    }

    /**
     * Animación para los switches cuando cambian de estado
     */
    private void animarSwitch(View switchView) {
        switchView.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .setInterpolator(new OvershootInterpolator(2f))
                .withEndAction(() -> {
                    switchView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void aplicarModoOscuro(boolean activar) {
        int modoDeseado = activar ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != modoDeseado) {
            AppCompatDelegate.setDefaultNightMode(modoDeseado);
            recreate();
        }
    }

    private void cerrarSesion() {
        // Animación de salida
        if (profileCard != null) {
            profileCard.animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        sessionManager.cerrarSesion();
                        PermissionManager.setUsuarioActual(null);
                        Intent intent = new Intent(this, ActivityLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        // Transición personalizada al cerrar sesión
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    })
                    .start();
        } else {
            sessionManager.cerrarSesion();
            PermissionManager.setUsuarioActual(null);
            Intent intent = new Intent(this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}