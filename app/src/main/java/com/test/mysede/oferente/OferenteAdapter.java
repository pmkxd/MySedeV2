package com.test.mysede.oferente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.OferenteActividad;

import java.util.List;

public class OferenteAdapter extends RecyclerView.Adapter<OferenteAdapter.ViewHolder> {

    private final Context context;
    private final List<OferenteActividad> listaOferentes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onVer(OferenteActividad oferente);
        void onEditar(OferenteActividad oferente, int position);
        void onEliminar(int position);
    }

    public OferenteAdapter(Context context, List<OferenteActividad> listaOferentes, OnItemClickListener listener) {
        this.context = context;
        this.listaOferentes = listaOferentes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_oferente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OferenteActividad oferente = listaOferentes.get(position);

        holder.tvNombre.setText(oferente.getNombre());

        holder.btnVer.setOnClickListener(v -> listener.onVer(oferente));
        holder.btnEditar.setOnClickListener(v -> listener.onEditar(oferente, position));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(position));
    }

    @Override
    public int getItemCount() {
        return listaOferentes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        ImageButton btnVer, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreOferente);
            btnVer = itemView.findViewById(R.id.btnVerOferente);
            btnEditar = itemView.findViewById(R.id.btnEditarOferente);
            btnEliminar = itemView.findViewById(R.id.btnEliminarOferente);
        }
    }
}
