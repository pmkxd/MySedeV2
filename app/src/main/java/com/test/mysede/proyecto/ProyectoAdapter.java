package com.test.mysede.proyecto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.Proyecto;

import java.util.List;

public class ProyectoAdapter extends RecyclerView.Adapter<ProyectoAdapter.ViewHolder> {

    private final Context context;
    private final List<Proyecto> listaProyectos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onVer(Proyecto proyecto);
        void onEditar(Proyecto proyecto, int position);
        void onEliminar(int position);
    }

    public ProyectoAdapter(Context context, List<Proyecto> listaProyectos, OnItemClickListener listener) {
        this.context = context;
        this.listaProyectos = listaProyectos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_proyecto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proyecto proyecto = listaProyectos.get(position);
        holder.tvNombreProyecto.setText(proyecto.getNombre());

        if (listener != null) {
            holder.btnVer.setOnClickListener(v -> listener.onVer(proyecto));
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(proyecto, position));
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(position));
        }
    }

    @Override
    public int getItemCount() {
        return listaProyectos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreProyecto;
        ImageButton btnVer, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreProyecto = itemView.findViewById(R.id.tvNombreProyecto);
            btnVer = itemView.findViewById(R.id.btnVerProyecto);
            btnEditar = itemView.findViewById(R.id.btnEditarProyecto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProyecto);
        }
    }
}
