package com.test.mysede.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.test.mysede.auth.SessionManager;
import com.test.mysede.login.ActivityLogin;
import com.test.mysede.model.Usuario;
import com.test.mysede.perfil.PerfilActivity; // Import nuevo para abrir el perfil
import com.test.mysede.perfil.ProfileImageLoader;

public class AdminActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private SessionManager sessionManager;
    private Usuario usuarioActual;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = new SessionManager(this);
        usuarioActual = PermissionManager.getUsuarioActual();
        if (usuarioActual == null) {
            usuarioActual = sessionManager.obtenerUsuarioSesion();
            if (usuarioActual != null) {
                PermissionManager.setUsuarioActual(usuarioActual);
            }
        }

        // Validar que el usuario tenga rol ADMINISTRADOR
        if (!PermissionManager.esAdministrador()) {
            Toast.makeText(this, "Acceso denegado. Solo administradores.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar Navigation (menú inferior)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_calendar,
                    R.id.nav_users,
                    R.id.nav_maintainers,
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin, menu);
        MenuItem item = menu.findItem(R.id.menu_perfil);
        ProfileImageLoader.loadIntoMenuItem(this, item,
                usuarioActual != null ? usuarioActual.getProfileImageUrl() : null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //  Nueva opción: abrir el perfil del usuario
        if (id == R.id.menu_perfil) {
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
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
