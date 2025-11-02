package com.test.mysede;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

final class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    interface OnCitaClickListener {
        void onCitaClick(@NonNull CalendarUiCita cita);
    }

    private final List<CalendarUiCita> data;
    private final OnCitaClickListener listener;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    CalendarDayAdapter(List<CalendarUiCita> data, OnCitaClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position), listener, timeFormatter);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    void actualizarDatos(@NonNull List<CalendarUiCita> citas) {
        data.clear();
        data.addAll(citas);
        notifyDataSetChanged();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView title;
        private final MaterialTextView subtitle;
        private final MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.calendar_event_title);
            subtitle = itemView.findViewById(R.id.calendar_event_subtitle);
            cardView = (MaterialCardView) itemView;
        }

        void bind(CalendarUiCita cita, OnCitaClickListener listener, DateTimeFormatter formatter) {
            String actividad = cita.getActividadNombre();
            if (actividad == null || actividad.trim().isEmpty()) {
                actividad = itemView.getResources().getString(R.string.calendario_evento_sin_actividad);
            }
            title.setText(actividad);
            subtitle.setText(itemView.getResources().getString(
                    R.string.calendario_evento_subtitulo,
                    cita.getLugar().getNombre(),
                    cita.getHora().format(formatter)
            ));
            cardView.setOnClickListener(v -> listener.onCitaClick(cita));
        }
    }
}