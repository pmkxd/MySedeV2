package com.test.mysede.notificaciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.Notificacion;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificacionClickListener {
        void onClick(Notificacion notificacion);
    }

    private final List<Notificacion> lista;
    private final OnNotificacionClickListener listener;

    public NotificationAdapter(List<Notificacion> lista, OnNotificacionClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion n = lista.get(position);

        holder.txtTitulo.setText(n.getTitulo());
        holder.txtMensaje.setText(n.getMensaje());
        holder.txtFecha.setText(n.getFecha());

        // Si ya fue leída, se ve más transparente
        holder.itemView.setAlpha(n.isLeida() ? 0.4f : 1f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitulo, txtMensaje, txtFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitulo = itemView.findViewById(R.id.txtNotiTitulo);
            txtMensaje = itemView.findViewById(R.id.txtNotiMensaje);
            txtFecha = itemView.findViewById(R.id.txtNotiFecha);
        }
    }
}
