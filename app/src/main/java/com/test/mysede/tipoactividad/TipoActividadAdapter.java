package com.test.mysede.tipoactividad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.TipoActividad;

import java.util.List;

public class TipoActividadAdapter extends RecyclerView.Adapter<TipoActividadAdapter.ViewHolder> {

    private final Context context;
    private final List<TipoActividad> listaTipos;
    private OnItemClickListener listener; //

    // 👇 nuevo constructor con listener
    public TipoActividadAdapter(Context context, List<TipoActividad> listaTipos, OnItemClickListener listener) {
        this.context = context;
        this.listaTipos = listaTipos;
        this.listener = listener;
    }

    // Interfaz para comunicar clics al Activity
    public interface OnItemClickListener {
        void onVer(TipoActividad tipo);
        void onEditar(TipoActividad tipo, int position);
        void onEliminar(int position);
    }

    public TipoActividadAdapter(Context context, List<TipoActividad> listaTipos) {
        this.context = context;
        this.listaTipos = listaTipos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tipo_actividad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TipoActividad tipo = listaTipos.get(position);
        holder.tvNombre.setText(tipo.getNombre());

        holder.btnVer.setOnClickListener(v -> listener.onVer(tipo));
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(tipo, position));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(position));
    }

    @Override
    public int getItemCount() {
        return listaTipos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        ImageButton btnVer, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreTipoActividad);
            btnVer = itemView.findViewById(R.id.btnVer);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
