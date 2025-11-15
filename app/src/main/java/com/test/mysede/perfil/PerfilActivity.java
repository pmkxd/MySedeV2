package com.test.mysede.perfil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.test.mysede.R;
import com.test.mysede.DAO.PerfilImagenDAO;
import com.test.mysede.DAO.UsuariosDAO;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.model.PerfilImagenResultado;
import com.test.mysede.model.Usuario;
import com.test.mysede.notificaciones.NotificacionesActivity;
import com.yalantis.ucrop.UCrop;

import java.io.File;

/**
 * Activity del perfil de usuario.
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
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private Usuario usuario;
    private PerfilImagenDAO perfilImagenDAO;
    private UsuariosDAO usuariosDAO;
    private ActivityResultLauncher<String> seleccionarImagenLauncher;
    private ActivityResultLauncher<Intent> recortarImagenLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        MaterialToolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


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

        // Abrir notificaciones
        btnVerNotificaciones.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacionesActivity.class))
        );
        // Abrir cambiar contraseña
        btnCambiarContrasena.setOnClickListener(v ->
                startActivity(new Intent(this, CambiarContrasenaActivity.class))
        );
        btnEditarFotoPerfil.setOnClickListener(v -> iniciarSeleccionImagen());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }
    private void configurarLaunchers() {
        seleccionarImagenLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                iniciarRecorte(uri);
            }
        });
        recortarImagenLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uriResultado = UCrop.getOutput(result.getData());
                if (uriResultado != null) {
                    subirNuevaImagen(uriResultado);
                } else {
                    Toast.makeText(this, R.string.perfil_error_procesar_imagen, Toast.LENGTH_SHORT).show();
                }
            } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                Throwable error = UCrop.getError(result.getData());
                String mensaje = error != null ? error.getMessage() : getString(R.string.perfil_error_procesar_imagen);
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarSeleccionImagen() {
        seleccionarImagenLauncher.launch("image/*");
    }

    private void iniciarRecorte(@NonNull Uri origen) {
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
                                Toast.makeText(this, R.string.perfil_imagen_actualizada, Toast.LENGTH_SHORT).show();
                                if (!TextUtils.isEmpty(tokenAnterior)) {
                                    perfilImagenDAO.eliminarAvatarPorToken(tokenAnterior);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, getString(R.string.perfil_error_guardar_imagen, e.getMessage()), Toast.LENGTH_SHORT).show();
                                restaurarAvatar();
                            })
                            .addOnCompleteListener(task -> finalizarActualizacion(uriRecortada));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.perfil_error_subir_imagen_detalle, e.getMessage()), Toast.LENGTH_SHORT).show();
                    finalizarActualizacion(uriRecortada);
                })
                .addOnCompleteListener(task -> eliminarTemporal(uriRecortada));
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

    private void mostrarCargaImagen(boolean mostrar) {
        progressAvatar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        progressAvatar.setIndeterminate(mostrar);
        imgPerfil.setAlpha(mostrar ? 0.5f : 1f);
    }

    private void configurarSwitchNotificaciones() {
        boolean notificacionesActivas = sharedPreferences.getBoolean(PREF_NOTIFICACIONES, true);
        switchNotificaciones.setChecked(notificacionesActivas);
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
            sharedPreferences.edit().putBoolean(PREF_MODO_OSCURO, isChecked).apply();
            aplicarModoOscuro(isChecked);
        });
    }
    private void aplicarModoOscuro(boolean activar) {
        int modoDeseado = activar ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != modoDeseado) {
            AppCompatDelegate.setDefaultNightMode(modoDeseado);
            recreate();
        }
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        PermissionManager.setUsuarioActual(null);
        Intent intent = new Intent(this, ActivityLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
