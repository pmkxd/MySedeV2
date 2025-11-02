package com.test.mysede;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class CalendarMonthAdapter extends RecyclerView.Adapter<CalendarMonthAdapter.MonthDayViewHolder> {

    interface OnDayClickListener {
        void onDayClicked(@NonNull LocalDate date);
    }

    private final List<MonthDay> days = new ArrayList<>();
    private final OnDayClickListener listener;
    private LocalDate selectedDate;

    CalendarMonthAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    void submitData(List<MonthDay> newDays, LocalDate selectedDate) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MonthDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_month_day, parent, false);
        return new MonthDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthDayViewHolder holder, int position) {
        holder.bind(days.get(position), selectedDate, listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static final class MonthDay {
        final LocalDate date;
        final boolean isInCurrentMonth;
        final int appointmentsCount;

        MonthDay(LocalDate date, boolean isInCurrentMonth, int appointmentsCount) {
            this.date = date;
            this.isInCurrentMonth = isInCurrentMonth;
            this.appointmentsCount = appointmentsCount;
        }
    }

    static final class MonthDayViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final MaterialTextView numberView;
        private final MaterialTextView indicatorView;

        MonthDayViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            numberView = itemView.findViewById(R.id.calendar_month_day_number);
            indicatorView = itemView.findViewById(R.id.calendar_month_day_indicator);
        }

        void bind(MonthDay day, LocalDate selectedDate, OnDayClickListener listener) {
            numberView.setText(String.valueOf(day.date.getDayOfMonth()));

            if (day.appointmentsCount > 0) {
                indicatorView.setText(String.valueOf(day.appointmentsCount));
                indicatorView.setVisibility(View.VISIBLE);
            } else {
                indicatorView.setVisibility(View.INVISIBLE);
            }

            card.setAlpha(day.isInCurrentMonth ? 1f : 0.4f);

            LocalDate today = LocalDate.now();
            boolean isToday = today.equals(day.date);
            boolean isSelected = selectedDate != null && selectedDate.equals(day.date);

            int strokeWidthRes = isToday ? R.dimen.calendar_month_day_today_stroke : R.dimen.calendar_month_day_default_stroke;
            int strokeColorRes = isToday ? R.color.md_theme_primary : R.color.md_theme_outlineVariant;
            card.setStrokeWidth(card.getResources().getDimensionPixelSize(strokeWidthRes));
            card.setStrokeColor(ContextCompat.getColor(card.getContext(), strokeColorRes));

            if (isSelected) {
                card.setCardBackgroundColor(ContextCompat.getColor(card.getContext(), R.color.md_theme_secondaryContainer));
            } else {
                card.setCardBackgroundColor(ContextCompat.getColor(card.getContext(), android.R.color.transparent));
            }

            card.setOnClickListener(v -> listener.onDayClicked(day.date));
        }
    }
}