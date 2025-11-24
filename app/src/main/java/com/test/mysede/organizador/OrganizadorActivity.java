package com.test.mysede.organizador;

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
import com.test.mysede.perfil.ProfileImageLoader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Rol;
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.model.Usuario;
import com.test.mysede.perfil.PerfilActivity; // ✅ Import para abrir el perfil

public class OrganizadorActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AppBarConfiguration appBarConfiguration;
    private Usuario usuarioActual;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizador);

        sessionManager = new SessionManager(this);
        usuarioActual = PermissionManager.getUsuarioActual();
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

        // Validar que el usuario tenga rol ORGANIZADOR_ACTIVIDADES
        if (!PermissionManager.tieneRol(Rol.ORGANIZADOR_ACTIVIDADES)) {
            Toast.makeText(this, "Acceso denegado. Solo organizadores de actividades.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        ProfileImageLoader.loadIntoMenuItem(this, menu.findItem(R.id.menu_perfil),
                usuarioActual != null ? usuarioActual.getProfileImageUrl() : null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //  Nuevo: abrir perfil
        if (id == R.id.menu_perfil) {
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Usuario actualizado = sessionManager.obtenerUsuarioSesion();
        if (actualizado != null) {
            usuarioActual = actualizado;
            PermissionManager.setUsuarioActual(actualizado);
        }
        invalidateOptionsMenu();
    }
}
