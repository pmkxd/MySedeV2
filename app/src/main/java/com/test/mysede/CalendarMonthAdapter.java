package com.test.mysede;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class CalendarMonthAdapter extends RecyclerView.Adapter<CalendarMonthAdapter.ViewHolder> {

    interface OnDayClickListener {
        void onDayClick(@NonNull LocalDate date);
    }

    private final List<CalendarMonthDay> days = new ArrayList<>();
    private final OnDayClickListener listener;

    CalendarMonthAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    void updateDays(@NonNull List<CalendarMonthDay> nuevosDias) {
        days.clear();
        days.addAll(nuevosDias);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_month_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(days.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final MaterialTextView dayNumber;
        private final MaterialTextView indicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            dayNumber = itemView.findViewById(R.id.calendar_month_day_number);
            indicator = itemView.findViewById(R.id.calendar_month_day_indicator);
        }

        void bind(CalendarMonthDay day, OnDayClickListener listener) {
            dayNumber.setText(String.valueOf(day.getDate().getDayOfMonth()));
            cardView.setOnClickListener(v -> listener.onDayClick(day.getDate()));

            float alpha = day.isCurrentMonth() ? 1f : 0.4f;
            cardView.setAlpha(alpha);

            int strokeWidth = day.isSelected() ? itemView.getResources().getDimensionPixelOffset(R.dimen.calendar_month_selected_stroke) : 0;
            cardView.setStrokeWidth(strokeWidth);
            int strokeColor = itemView.getResources().getColor(R.color.md_theme_primary, itemView.getContext().getTheme());
            cardView.setStrokeColor(strokeColor);

            if (day.isToday()) {
                int color = itemView.getResources().getColor(R.color.md_theme_primary, itemView.getContext().getTheme());
                dayNumber.setTextColor(color);
            } else {
                int color = itemView.getResources().getColor(R.color.md_theme_onSurface, itemView.getContext().getTheme());
                dayNumber.setTextColor(color);
            }

            if (day.getAppointmentsCount() > 0) {
                indicator.setVisibility(View.VISIBLE);
                indicator.setText(String.valueOf(day.getAppointmentsCount()));
            } else {
                indicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    static final class CalendarMonthDay {
        private final LocalDate date;
        private final boolean currentMonth;
        private final boolean today;
        private final boolean selected;
        private final int appointmentsCount;

        CalendarMonthDay(LocalDate date, boolean currentMonth, boolean today, boolean selected, int appointmentsCount) {
            this.date = date;
            this.currentMonth = currentMonth;
            this.today = today;
            this.selected = selected;
            this.appointmentsCount = appointmentsCount;
        }

        LocalDate getDate() {
            return date;
        }

        boolean isCurrentMonth() {
            return currentMonth;
        }

        boolean isToday() {
            return today;
        }

        boolean isSelected() {
            return selected;
        }

        int getAppointmentsCount() {
            return appointmentsCount;
        }
    }
}