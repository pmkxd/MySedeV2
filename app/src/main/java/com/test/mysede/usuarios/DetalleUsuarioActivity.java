package com.test.mysede.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_usuario);

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
        } else {
            // Si no hay usuario, cerrar la activity
            finish();
        }

        // Configurar botón editar
        if (PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO)) {
            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(this, CrearUsuarioActivity.class);
                intent.putExtra("usuario", usuario);
                intent.putExtra("modo", "editar");
                startActivity(intent);
                finish(); // Cerrar para refrescar al volver
            });
        } else {
            btnEditar.setVisibility(android.view.View.GONE);
        }
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
                    layoutPermisos.addView(tvCategoria);

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
                            layoutPermisos.addView(tvPermiso);
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
            layoutPermisos.addView(tvSinPermisos);
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