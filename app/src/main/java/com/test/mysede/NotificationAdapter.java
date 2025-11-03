package com.test.mysede;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.model.Notificacion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private ArrayList<Notificacion> notificaciones;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Notificacion n);
    }

    public NotificationAdapter(ArrayList<Notificacion> notificaciones, OnItemClickListener listener) {
        this.notificaciones = notificaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion n = notificaciones.get(position);

        holder.titulo.setText(n.getTitulo());
        holder.mensaje.setText(n.getMensaje());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        holder.fecha.setText(sdf.format(n.getFecha().toDate()));

        if (!n.isLeida()) {
            holder.card.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.md_theme_surfaceContainerHigh));
        } else {
            holder.card.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.md_theme_surfaceContainerLow));
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(n));
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, mensaje, fecha;
        CardView card;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txtNotiTitulo);
            mensaje = itemView.findViewById(R.id.txtNotiMensaje);
            fecha = itemView.findViewById(R.id.txtNotiFecha);
            card = itemView.findViewById(R.id.cardNoti);
        }
    }
}
