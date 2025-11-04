package com.test.mysede;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.test.mysede.model.Cita;
import com.test.mysede.model.sample.CitaSamples;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

    public class CalendarActivity extends AppCompatActivity implements CalendarWeekAdapter.OnEventInteractionListener {

        private final Locale locale = new Locale("es", "ES");
        private final DateTimeFormatter dayTitleFormatter = DateTimeFormatter.ofPattern("EEE d", locale);
        private final DateTimeFormatter dateDetailFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale);
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

        private CoordinatorLayout root;
        private MaterialTextView weekTitle;
        private RecyclerView weekRecycler;
        private MaterialButton previousWeekButton;
        private MaterialButton nextWeekButton;

        private LocalDate currentWeekStart;
        private final List<CalendarUiCita> allAppointments = new ArrayList<>();
        private final List<DaySchedule> currentWeekDays = new ArrayList<>();
        private CalendarWeekAdapter adapter;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_calendar);
            root = findViewById(R.id.calendar_root);
            weekTitle = findViewById(R.id.calendar_week_title);
            weekRecycler = findViewById(R.id.calendar_week_recycler);
            previousWeekButton = findViewById(R.id.calendar_week_previous);
            nextWeekButton = findViewById(R.id.calendar_week_next);
            ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsets;
            });

            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> finish());

            adapter = new CalendarWeekAdapter(currentWeekDays, dayTitleFormatter, this);
            weekRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            weekRecycler.setAdapter(adapter);

            previousWeekButton.setOnClickListener(v -> mostrarSemana(currentWeekStart.minusWeeks(1)));
            nextWeekButton.setOnClickListener(v -> mostrarSemana(currentWeekStart.plusWeeks(1)));

            cargarDatosDemo();
            mostrarSemana(LocalDate.now());
            }
        private void cargarDatosDemo() {
            allAppointments.clear();
            for (Cita cita : CitaSamples.citasSemanaDemostracion()) {
                CalendarUiCita uiCita = new CalendarUiCita(cita, 60);
                allAppointments.add(uiCita);
            }
        }

        private void mostrarSemana(LocalDate referenceDate) {
            currentWeekStart = referenceDate.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = currentWeekStart.plusDays(6);

            currentWeekDays.clear();
            for (int i = 0; i < 7; i++) {
                currentWeekDays.add(new DaySchedule(currentWeekStart.plusDays(i)));
            }
            for (CalendarUiCita cita : allAppointments) {
                if (!cita.getFecha().isBefore(currentWeekStart) && !cita.getFecha().isAfter(endOfWeek)) {
                    DaySchedule dia = buscarDia(cita.getFecha());
                    if (dia != null) {
                        dia.addAppointment(cita);
                    }
                }
            }
            String titleText = capitalizar(currentWeekStart.format(dateDetailFormatter))
                    + " - "
                    + capitalizar(endOfWeek.format(dateDetailFormatter));
            weekTitle.setText(titleText);
            adapter.notifyDataSetChanged();
            if (adapter.getItemCount() > 0) {
                int position = Math.min(Math.max(referenceDate.getDayOfWeek().getValue() - 1, 0), adapter.getItemCount() - 1);
                weekRecycler.scrollToPosition(position);
            }
        }
        @Nullable
        private DaySchedule buscarDia(LocalDate date) {
            for (DaySchedule day : currentWeekDays) {
                if (day.getDate().equals(date)) {
                    return day;
                }
            }
            return null;
        }
        private void reagendarCita(CalendarUiCita cita, LocalDate nuevaFecha, LocalTime nuevaHora) {
            Objects.requireNonNull(cita, "La cita es obligatoria");
            Objects.requireNonNull(nuevaFecha, "La fecha es obligatoria");
            Objects.requireNonNull(nuevaHora, "La hora es obligatoria");

            DaySchedule origen = buscarDia(cita.getFecha());
            if (origen != null) {
                origen.removeAppointment(cita);
            }

            cita.actualizarFechaHora(nuevaFecha, nuevaHora);

            if (nuevaFecha.isBefore(currentWeekStart) || nuevaFecha.isAfter(currentWeekStart.plusDays(6))) {
                mostrarSemana(nuevaFecha);
            } else {
                DaySchedule destino = buscarDia(nuevaFecha);
                if (destino != null) {
                    destino.addAppointment(cita);
                }
                adapter.notifyDataSetChanged();
            }

            Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada, cita.getActividad().getNombre(), capitalizar(nuevaFecha.format(dateDetailFormatter)), nuevaHora.format(timeFormatter)), Snackbar.LENGTH_LONG).show();
        }
        private void eliminarCita(CalendarUiCita cita) {
            DaySchedule dia = buscarDia(cita.getFecha());
            if (dia != null) {
                dia.removeAppointment(cita);
            }
            allAppointments.remove(cita);
            adapter.notifyDataSetChanged();
            Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, cita.getActividad().getNombre()), Snackbar.LENGTH_LONG).show();
        }
        private void mostrarDialogoDetalle(CalendarUiCita cita) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_cita_detalle, null, false);
            MaterialTextView titulo = view.findViewById(R.id.calendario_detalle_titulo);
            MaterialTextView horario = view.findViewById(R.id.calendario_detalle_horario);
            MaterialTextView descripcion = view.findViewById(R.id.calendario_detalle_descripcion);
            MaterialSwitch notificarSwitch = view.findViewById(R.id.calendario_detalle_switch_notificar);
            MaterialButton reagendarButton = view.findViewById(R.id.calendario_detalle_btn_reagendar);
            MaterialButton eliminarButton = view.findViewById(R.id.calendario_detalle_btn_eliminar);

            titulo.setText(cita.getActividad().getNombre());
            horario.setText(getString(R.string.calendario_detalle_horario, capitalizar(cita.getFecha().format(dateDetailFormatter)), cita.getHora().format(timeFormatter)));
            descripcion.setText(getString(R.string.calendario_detalle_lugar, cita.getLugar().getNombre()));

            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setView(view)
                    .create();

            reagendarButton.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoReagendar(cita, notificarSwitch.isChecked());
            });
            eliminarButton.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoEliminar(cita, notificarSwitch.isChecked());
            });

            dialog.show();
        }
        private void mostrarDialogoReagendar(CalendarUiCita cita, boolean notificar) {
            long seleccionInicial = cita.getFecha().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.calendario_dialogo_reagendar_fecha)
                    .setSelection(seleccionInicial)
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection == null) {
                    return;
                }
                LocalDate nuevaFecha = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate();
                mostrarSelectorHora(cita, nuevaFecha, notificar);
            });
            datePicker.show(getSupportFragmentManager(), "calendario_reagendar_fecha");
        }
        private String capitalizar(String texto) {
            if (TextUtils.isEmpty(texto)) {
                return texto;
            }
            return texto.substring(0, 1).toUpperCase(locale) + texto.substring(1);
        }
        private void mostrarSelectorHora(CalendarUiCita cita, LocalDate nuevaFecha, boolean notificar) {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(cita.getHora().getHour())
                    .setMinute(cita.getHora().getMinute())
                    .setTitleText(R.string.calendario_dialogo_reagendar_hora)
                    .build();
            picker.addOnPositiveButtonClickListener(v -> {
                LocalTime nuevaHora = LocalTime.of(picker.getHour(), picker.getMinute());
                reagendarCita(cita, nuevaFecha, nuevaHora);
                if (notificar) {
                    Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
                }
            });
            picker.show(getSupportFragmentManager(), "calendario_reagendar_hora");
        }
        private void mostrarDialogoEliminar(CalendarUiCita cita, boolean notificar) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.calendario_dialogo_eliminar_titulo)
                    .setMessage(getString(R.string.calendario_dialogo_eliminar_mensaje, cita.getActividad().getNombre()))
                    .setPositiveButton(R.string.calendario_dialogo_eliminar_confirmar, (dialog, which) -> {
                        eliminarCita(cita);
                        if (notificar) {
                            Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
        @Override
        public void onEventSelected(@NonNull CalendarUiCita cita) {
            mostrarDialogoDetalle(cita);
        }
        @Override
        public void onEventDropped(@NonNull CalendarUiCita cita, @NonNull LocalDate nuevaFecha) {
            reagendarCita(cita, nuevaFecha, cita.getHora());
        }

    }