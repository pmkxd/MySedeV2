package com.test.mysede;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class CalendarDayAppointmentsAdapter extends RecyclerView.Adapter<CalendarDayAppointmentsAdapter.ViewHolder> {

    interface OnAppointmentClickListener {
        void onAppointmentClick(@NonNull CalendarUiCita cita);
    }

    private final List<CalendarUiCita> citas = new ArrayList<>();
    private final OnAppointmentClickListener listener;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    CalendarDayAppointmentsAdapter(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    void submitList(List<CalendarUiCita> nuevasCitas) {
        citas.clear();
        if (nuevasCitas != null) {
            citas.addAll(nuevasCitas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(citas.get(position), listener, timeFormatter);
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final MaterialTextView titleView;
        private final MaterialTextView subtitleView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            titleView = itemView.findViewById(R.id.calendar_event_title);
            subtitleView = itemView.findViewById(R.id.calendar_event_subtitle);
        }

        void bind(CalendarUiCita cita, OnAppointmentClickListener listener, DateTimeFormatter timeFormatter) {
            titleView.setText(cita.getActividadNombre());
            subtitleView.setText(itemView.getResources().getString(R.string.calendario_evento_subtitulo,
                    cita.getLugarNombre(),
                    cita.getHora().format(timeFormatter)));
            card.setOnClickListener(v -> listener.onAppointmentClick(cita));
            card.setOnLongClickListener(null);
        }
    }
}