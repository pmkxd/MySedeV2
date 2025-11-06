package com.test.mysede.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.mysede.DAO.ActividadDAO;
import com.test.mysede.R;
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import com.test.mysede.model.Actividad;

import java.util.ArrayList;
import java.util.List;

public class ListarActividadesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActividadAdapter adapter;
    private FloatingActionButton fabCrear;
    private final List<Actividad> actividades = new ArrayList<>();
    private final ActividadDAO actividadDAO = new ActividadDAO();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listar_actividades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!PermissionManager.tienePermiso(Permiso.VER_ACTIVIDADES)) {
            Toast.makeText(requireContext(), "No tienes permiso para ver actividades", Toast.LENGTH_SHORT).show();
            view.setVisibility(View.GONE);
            return;
        }

        recyclerView = view.findViewById(R.id.recyclerViewActividades);
        fabCrear = view.findViewById(R.id.fabCrearActividad);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ActividadAdapter(requireContext(), actividades, actividad -> {
            Intent intent = new Intent(requireContext(), VerActividadActivity.class);
            intent.putExtra("actividadId", actividad.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        if (PermissionManager.tienePermiso(Permiso.CREAR_ACTIVIDAD)) {
            fabCrear.setVisibility(View.VISIBLE);
            fabCrear.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CrearActividadActivity.class);
                startActivity(intent);
            });
        } else {
            fabCrear.setVisibility(View.GONE);
        }

        cargarActividades();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PermissionManager.tienePermiso(Permiso.VER_ACTIVIDADES)) {
            cargarActividades();
        }
    }

    private void cargarActividades() {
        actividadDAO.getAllActividades(new ActividadDAO.OnActividadesLoadedListener() {
            @Override
            public void onActividadesLoaded(ArrayList<Actividad> actividadesCargadas) {
                if (!isAdded()) return;
                actividades.clear();
                if (actividadesCargadas != null) {
                    actividades.addAll(actividadesCargadas);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
            }
        });
    }
}