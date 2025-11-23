package com.test.mysede.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.mysede.R;
import com.test.mysede.DAO.UsuariosDAO;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class GestionUsuariosFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsuarioAdapter adapter;
    private FloatingActionButton fabCrear;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private UsuariosDAO usuariosDAO;
    private List<Usuario> usuariosList;
    private FirebaseAuth mAuth;

    // 🔥 ID del usuario que inició sesión (el admin real)
    private String adminRealId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gestion_usuarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!PermissionManager.tienePermiso(Permiso.VER_USUARIOS)) {
            Toast.makeText(requireContext(), "No tienes permiso para acceder a esta sección", Toast.LENGTH_SHORT).show();
            view.setVisibility(View.GONE);
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        usuariosDAO = new UsuariosDAO();
        usuariosList = new ArrayList<>();

        // 🔥 Guardar el ID del admin real desde PermissionManager
        Usuario adminActual = PermissionManager.getUsuarioActual();
        if (adminActual != null) {
            adminRealId = adminActual.getId();
        }

        recyclerView = view.findViewById(R.id.recyclerViewUsuarios);
        fabCrear = view.findViewById(R.id.fabCrearUsuario);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cargarUsuarios();

        if (PermissionManager.tienePermiso(Permiso.CREAR_USUARIO)) {
            fabCrear.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CrearUsuarioActivity.class);
                startActivity(intent);
            });
        } else {
            fabCrear.hide();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PermissionManager.tienePermiso(Permiso.VER_USUARIOS)) {
            cargarUsuarios();
        }
    }

    private void cargarUsuarios() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        usuariosDAO.getAllUsuarios(new UsuariosDAO.OnUsuariosLoadedListener() {
            @Override
            public void onUsuariosLoaded(ArrayList<Usuario> usuarios) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                // Filtrar solo usuarios activos
                ArrayList<Usuario> usuariosActivos = new ArrayList<>();
                for (Usuario usuario : usuarios) {
                    if (usuario.isActivo()) {
                        usuariosActivos.add(usuario);
                    }
                }
                usuariosList = usuariosActivos;

                if (usuariosList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    adapter = new UsuarioAdapter(requireContext(), usuariosList, (usuario, posicion) -> mostrarOpcionesUsuario(usuario, posicion));
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                tvEmpty.setText("Error al cargar usuarios. Por favor, intenta nuevamente.");
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarOpcionesUsuario(Usuario usuario, int posicion) {
        String[] opciones;

        if (PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO) &&
                PermissionManager.tienePermiso(Permiso.ELIMINAR_USUARIO)) {
            opciones = new String[]{"Ver Detalles", "Editar", "Deshabilitar"};
        } else if (PermissionManager.tienePermiso(Permiso.EDITAR_USUARIO)) {
            opciones = new String[]{"Ver Detalles", "Editar"};
        } else {
            opciones = new String[]{"Ver Detalles"};
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(usuario.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (opciones[which]) {
                        case "Ver Detalles":
                            verDetalles(usuario);
                            break;
                        case "Editar":
                            editarUsuario(usuario);
                            break;
                        case "Deshabilitar":
                            confirmarEliminar(usuario, posicion);
                            break;
                    }
                })
                .show();
    }

    private void verDetalles(Usuario usuario) {
        Intent intent = new Intent(requireContext(), DetalleUsuarioActivity.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }

    private void editarUsuario(Usuario usuario) {
        Intent intent = new Intent(requireContext(), CrearUsuarioActivity.class);
        intent.putExtra("usuario", usuario);
        intent.putExtra("modo", "editar");
        startActivity(intent);
    }

    private void confirmarEliminar(Usuario usuario, int posicion) {

        // Usar SIEMPRE PermissionManager
        Usuario adminActual = PermissionManager.getUsuarioActual();

        if (adminActual != null && usuario.getId().equals(adminActual.getId())) {
            Toast.makeText(requireContext(),
                    "No puedes deshabilitar tu propio usuario",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Deshabilitar Usuario")
                .setMessage("¿Está seguro que desea deshabilitar a " + usuario.getNombre() + "?\n\n" +
                        "El usuario quedará inactivo y no podrá iniciar sesión.\n" +
                        "Podrás reactivarlo más tarde si es necesario.")
                .setPositiveButton("Deshabilitar", (dialog, which) ->
                        deshabilitarUsuario(usuario, posicion))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void deshabilitarUsuario(Usuario usuario, int posicion) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Marcar como inactivo en lugar de eliminar
        db.collection("usuarios")
                .document(usuario.getId())
                .update("activo", false)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(requireContext(),
                            "Usuario deshabilitado correctamente",
                            Toast.LENGTH_SHORT).show();

                    // Actualizar el estado local
                    usuario.setActivo(false);

                    progressBar.setVisibility(View.GONE);
                    cargarUsuarios();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error al deshabilitar usuario: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}