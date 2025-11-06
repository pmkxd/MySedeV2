package com.test.mysede.mantenedores;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.test.mysede.R;
import com.test.mysede.lugar.LugarActivity;
import com.test.mysede.oferente.OferenteActivity;
import com.test.mysede.proyecto.ProyectoActivity;
import com.test.mysede.socio.SocioComunitarioActivity;
import com.test.mysede.tipoactividad.TipoActividadActivity;

public class MantenedoresFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mantenedores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialCardView cardTipoActividad = view.findViewById(R.id.card_tipo_actividad);
        MaterialCardView cardLugares = view.findViewById(R.id.card_lugares);
        MaterialCardView cardOferentes = view.findViewById(R.id.card_oferentes);
        MaterialCardView cardSocios = view.findViewById(R.id.card_socios);
        MaterialCardView cardProyectos = view.findViewById(R.id.card_proyectos);

        cardTipoActividad.setOnClickListener(v -> startActivity(new Intent(requireContext(), TipoActividadActivity.class)));
        cardLugares.setOnClickListener(v -> startActivity(new Intent(requireContext(), LugarActivity.class)));
        cardOferentes.setOnClickListener(v -> startActivity(new Intent(requireContext(), OferenteActivity.class)));
        cardSocios.setOnClickListener(v -> startActivity(new Intent(requireContext(), SocioComunitarioActivity.class)));
        cardProyectos.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProyectoActivity.class)));
    }
}