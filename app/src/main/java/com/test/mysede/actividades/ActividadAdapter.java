package com.test.mysede.actividades;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.TipoActividad;

import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    private final Context context;
    private List<Actividad> actividades;
    private final OnActividadClickListener listener;

    public ActividadAdapter(Context context, List<Actividad> actividades) {
        this(context, actividades, null);
    }

    public ActividadAdapter(Context context, List<Actividad> actividades, OnActividadClickListener listener) {
        this.context = context;
        this.actividades = actividades;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_actividad, parent, false);
        return new ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Actividad actividad = actividades.get(position);

        holder.tvNombre.setText(actividad.getNombre());

        // Mostrar tipo de actividad
        if (!actividad.getTiposActividad().isEmpty()) {
            TipoActividad tipo = actividad.getTiposActividad().get(0);
            holder.tvTipo.setText(tipo.getNombre());
        } else {
            holder.tvTipo.setText("Sin tipo");
        }

        // Mostrar periodicidad
        holder.tvPeriodicidad.setText(actividad.getPeriodicidad().getNombre());

        // Mostrar cupo
        if (actividad.getCupo() != null) {
            holder.tvCupo.setText("Cupo: " + actividad.getCupo());
        } else {
            holder.tvCupo.setText("Sin cupo definido");
        }

        // Mostrar proyecto
        if (actividad.getProyecto() != null) {
            holder.tvProyecto.setText(actividad.getProyecto().getNombre());
        } else {
            holder.tvProyecto.setText("Sin proyecto");
        }

        // Click para ver detalle
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActividadClick(actividad);
            } else {
                Intent intent = new Intent(context, VerActividadActivity.class);
                intent.putExtra("actividadId", actividad.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return actividades.size();
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
        notifyDataSetChanged();
    }

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }
    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvTipo;
        TextView tvPeriodicidad;
        TextView tvCupo;
        TextView tvProyecto;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreActividad);
            tvTipo = itemView.findViewById(R.id.tvTipoActividad);
            tvPeriodicidad = itemView.findViewById(R.id.tvPeriodicidadActividad);
            tvCupo = itemView.findViewById(R.id.tvCupoActividad);
            tvProyecto = itemView.findViewById(R.id.tvProyectoActividad);
        }
    }
}