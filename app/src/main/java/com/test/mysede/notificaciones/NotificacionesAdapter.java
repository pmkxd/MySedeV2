package com.test.mysede.notificaciones;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

    private List<Notificacion> lista;
    private OnNotificacionClickListener listener;
    private SimpleDateFormat sdf;

    // Interfaz para manejar clics
    public interface OnNotificacionClickListener {
        void onNotificacionClick(Notificacion notificacion);
    }

    public NotificacionesAdapter(List<Notificacion> lista, OnNotificacionClickListener listener) {
        this.lista = lista;
        this.listener = listener;
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion n = lista.get(position);

        holder.txtTitulo.setText(n.getTitulo());
        holder.txtMensaje.setText(n.getMensaje());
        holder.txtFecha.setText(formatearFechaAmigable(n.getFechaHora()));

        // Estilo según si está leída o no
        if (!n.isLeida()) {
            // No leída: fondo azul claro y texto en negrita
            holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
            holder.txtTitulo.setTypeface(null, Typeface.BOLD);
            if (holder.indicadorNoLeida != null) {
                holder.indicadorNoLeida.setVisibility(View.VISIBLE);
            }
        } else {
            // Leída: fondo blanco y texto normal
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.txtTitulo.setTypeface(null, Typeface.NORMAL);
            if (holder.indicadorNoLeida != null) {
                holder.indicadorNoLeida.setVisibility(View.GONE);
            }
        }

        // Manejar clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificacionClick(n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    /**
     * Formatear fecha de forma amigable (hace X tiempo)
     */
    private String formatearFechaAmigable(long timestamp) {
        long ahora = System.currentTimeMillis();
        long diferencia = ahora - timestamp;

        long segundos = diferencia / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        if (minutos < 1) {
            return "Ahora";
        } else if (minutos < 60) {
            return "Hace " + minutos + " min";
        } else if (horas < 24) {
            return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (dias < 7) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        } else {
            return sdf.format(timestamp);
        }
    }

    /**
     * Actualizar lista de notificaciones
     */
    public void actualizarLista(List<Notificacion> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtMensaje, txtFecha;
        View indicadorNoLeida;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtMensaje = itemView.findViewById(R.id.txtMensaje);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            indicadorNoLeida = itemView.findViewById(R.id.indicadorNoLeida);
        }
    }
}