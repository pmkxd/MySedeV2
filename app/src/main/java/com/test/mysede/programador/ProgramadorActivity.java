package com.test.mysede.programador;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Rol;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.model.Usuario;

public class ProgramadorActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programador);

        sessionManager = new SessionManager(this);
        Usuario usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            usuarioActual = sessionManager.obtenerUsuarioSesion();
            if (usuarioActual != null) {
                PermissionManager.setUsuarioActual(usuarioActual);
            }
        }
        if (usuarioActual == null) {
            Toast.makeText(this, "Sesión inválida, por favor ingresa nuevamente", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        // Validar que el usuario tenga rol PROGRAMADOR_CITAS
        if (!PermissionManager.tieneRol(Rol.PROGRAMADOR_CITAS)) {
            Toast.makeText(this, "Acceso denegado. Solo programadores de citas.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(usuarioActual.getNombre());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_calendar,
                    R.id.nav_activities
            ).build();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cerrar_sesion) {
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
