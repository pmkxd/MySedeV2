package com.test.mysede.lugar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.Lugar;

import java.util.List;

public class LugarAdapter extends RecyclerView.Adapter<LugarAdapter.ViewHolder> {

    private final Context context;
    private final List<Lugar> listaLugares;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onVer(Lugar lugar);
        void onEditar(Lugar lugar, int position);
        void onEliminar(int position);
    }

    public LugarAdapter(Context context, List<Lugar> listaLugares, OnItemClickListener listener) {
        this.context = context;
        this.listaLugares = listaLugares;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lugar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lugar lugar = listaLugares.get(position);

        holder.tvNombreLugar.setText(lugar.getNombre());
        holder.tvTipoLugar.setText("Tipo: " + lugar.getTipo().toString().replace("_", " "));
        holder.tvCupoLugar.setText(lugar.getCupo().isPresent() ? "Cupo: " + lugar.getCupo().get() : "Cupo: No especificado");

        holder.btnVer.setOnClickListener(v -> listener.onVer(lugar));
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(lugar, position));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(position));
    }

    @Override
    public int getItemCount() {
        return listaLugares.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreLugar, tvTipoLugar, tvCupoLugar;
        ImageButton btnVer, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreLugar = itemView.findViewById(R.id.tvNombreLugar);
            tvTipoLugar = itemView.findViewById(R.id.tvTipoLugar);
            tvCupoLugar = itemView.findViewById(R.id.tvCupoLugar);
            btnVer = itemView.findViewById(R.id.btnVerLugar);
            btnEditar = itemView.findViewById(R.id.btnEditarLugar);
            btnEliminar = itemView.findViewById(R.id.btnEliminarLugar);
        }
    }
}
