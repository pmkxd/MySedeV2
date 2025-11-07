package com.test.mysede.calendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class DaySchedule {
    private final LocalDate date;
    private final List<CalendarUiCita> citas = new ArrayList<>();

    DaySchedule(LocalDate date) {
        this.date = date;
    }

    LocalDate getDate() {
        return date;
    }

    List<CalendarUiCita> getCitas() {
        return citas;
    }

    void addAppointment(CalendarUiCita cita) {
        if (!citas.contains(cita)) {
            citas.add(cita);
        }
        citas.sort(Comparator.comparing(CalendarUiCita::getHora));
    }

    void removeAppointment(CalendarUiCita cita) {
        citas.remove(cita);
    }
}