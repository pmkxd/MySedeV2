package com.test.mysede.socio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.SocioComunitario;

import java.util.List;

public class SocioComunitarioAdapter extends RecyclerView.Adapter<SocioComunitarioAdapter.ViewHolder> {

    private final Context context;
    private final List<SocioComunitario> listaSocios;
    private OnItemClickListener listener;

    // 🔹 Interfaz para eventos
    public interface OnItemClickListener {
        void onVer(SocioComunitario socio);
        void onEditar(SocioComunitario socio, int position);
        void onEliminar(int position);
    }

    // 🔹 Constructor
    public SocioComunitarioAdapter(Context context, List<SocioComunitario> listaSocios, OnItemClickListener listener) {
        this.context = context;
        this.listaSocios = listaSocios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_socio_comunitario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SocioComunitario socio = listaSocios.get(position);

        holder.tvNombreSocio.setText(socio.getNombre());
        holder.tvCantBeneficiarios.setText("Beneficiarios: " + socio.getBeneficiarios().size());

        if (listener != null) {
            holder.btnVer.setOnClickListener(v -> listener.onVer(socio));
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(socio, position));
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(position));
        }
    }

    @Override
    public int getItemCount() {
        return listaSocios.size();
    }

    // 🔹 ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreSocio, tvCantBeneficiarios;
        ImageButton btnVer, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreSocio = itemView.findViewById(R.id.tvNombreSocio);
            tvCantBeneficiarios = itemView.findViewById(R.id.tvCantBeneficiarios);
            btnVer = itemView.findViewById(R.id.btnVerSocio);
            btnEditar = itemView.findViewById(R.id.btnEditarSocio);
            btnEliminar = itemView.findViewById(R.id.btnEliminarSocio);
        }
    }
}
