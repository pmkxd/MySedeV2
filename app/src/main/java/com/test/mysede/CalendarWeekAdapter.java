package com.test.mysede;

import android.content.ClipData;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

final class CalendarWeekAdapter extends RecyclerView.Adapter<CalendarWeekAdapter.DayViewHolder> {

    interface OnEventInteractionListener {
        void onEventSelected(@NonNull CalendarUiCita cita);
        void onEventDropped(@NonNull CalendarUiCita cita, @NonNull LocalDate nuevaFecha);
    }

    private final List<DaySchedule> data;
    private final DateTimeFormatter titleFormatter;
    private final OnEventInteractionListener listener;

    CalendarWeekAdapter(List<DaySchedule> data, DateTimeFormatter titleFormatter, OnEventInteractionListener listener) {
        this.data = data;
        this.titleFormatter = titleFormatter;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(data.get(position), titleFormatter, listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static final class DayViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView title;
        private final MaterialTextView emptyText;
        private final LinearLayout container;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.calendar_day_title);
            emptyText = itemView.findViewById(R.id.calendar_day_empty);
            container = itemView.findViewById(R.id.calendar_day_events);
        }

        void bind(DaySchedule schedule, DateTimeFormatter formatter, OnEventInteractionListener listener) {
            Locale locale = new Locale("es", "ES");
            String formatted = schedule.getDate().format(formatter);
            if (!TextUtils.isEmpty(formatted)) {
                title.setText(formatted.substring(0, 1).toUpperCase(locale) + formatted.substring(1));
            } else {
                title.setText(String.valueOf(schedule.getDate().getDayOfMonth()));
            }

            container.removeAllViews();
            for (CalendarUiCita cita : schedule.getCitas()) {
                View card = crearVistaEvento(container, cita, listener);
                container.addView(card);
            }

            emptyText.setVisibility(schedule.getCitas().isEmpty() ? View.VISIBLE : View.GONE);

            container.setOnDragListener((v, event) -> manejarDrag(event, schedule.getDate(), listener));
        }

        private boolean manejarDrag(DragEvent event, LocalDate date, OnEventInteractionListener listener) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getLocalState() instanceof CalendarUiCita;
                case DragEvent.ACTION_DRAG_ENTERED:
                    container.setBackgroundResource(R.drawable.bg_calendar_drop_target);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    container.setBackgroundResource(0);
                    return true;
                case DragEvent.ACTION_DROP:
                    container.setBackgroundResource(0);
                    Object localState = event.getLocalState();
                    if (localState instanceof CalendarUiCita) {
                        listener.onEventDropped((CalendarUiCita) localState, date);
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENDED:
                    container.setBackgroundResource(0);
                    return true;
                default:
                    return false;
            }
        }

        private View crearVistaEvento(ViewGroup parent, CalendarUiCita cita, OnEventInteractionListener listener) {
            MaterialCardView card = (MaterialCardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_event, parent, false);
            MaterialTextView title = card.findViewById(R.id.calendar_event_title);
            MaterialTextView subtitle = card.findViewById(R.id.calendar_event_subtitle);
            title.setText(cita.getActividadNombre());
            subtitle.setText(parent.getResources().getString(R.string.calendario_evento_subtitulo,
                    cita.getLugarNombre(),
                    cita.getHora().format(DateTimeFormatter.ofPattern("HH:mm"))));
            card.setOnClickListener(v -> listener.onEventSelected(cita));
            card.setOnLongClickListener(v -> {
                ClipData data = ClipData.newPlainText("cita", cita.getUiId().toString());
                v.startDragAndDrop(data, new View.DragShadowBuilder(v), cita, 0);
                return true;
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int margin = Math.round(parent.getResources().getDimension(R.dimen.calendar_event_margin));
            params.setMargins(0, margin, 0, margin);
            card.setLayoutParams(params);


            return card;
        }
    }
}