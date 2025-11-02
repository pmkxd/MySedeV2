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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements CalendarDayAdapter.OnCitaClickListener {

    private final Locale locale = new Locale("es", "ES");
    private final DateTimeFormatter dayTitleFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale);
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale);
    private final DateTimeFormatter dateDetailFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    private CoordinatorLayout root;
    private MaterialButtonToggleGroup toggleGroup;
    private View dayContainer;
    private View monthContainer;
    private MaterialTextView dayTitle;
    private MaterialTextView dayEmpty;
    private MaterialButton previousDayButton;
    private MaterialButton nextDayButton;
    private MaterialButton previousMonthButton;
    private MaterialButton nextMonthButton;
    private MaterialTextView monthTitle;
    private androidx.recyclerview.widget.RecyclerView dayList;
    private androidx.recyclerview.widget.RecyclerView monthGrid;

    private CalendarDayAdapter dayAdapter;
    private CalendarMonthAdapter monthAdapter;

    private final List<CalendarUiCita> todasLasCitas = new ArrayList<>();
    private final Map<LocalDate, List<CalendarUiCita>> citasPorDia = new HashMap<>();
    private final List<CalendarUiCita> citasDelDia = new ArrayList<>();

    private final CalendarRepository repository = new CalendarRepository();

    private LocalDate fechaSeleccionada = LocalDate.now();
    private YearMonth mesActual = YearMonth.now();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);
        inicializarVista();
        configurarToolbar();
        configurarToggle();
        configurarListados();
        cargarCitas();
    }
    private void inicializarVista() {
        root = findViewById(R.id.calendar_root);
        toggleGroup = findViewById(R.id.calendar_toggle_group);
        dayContainer = findViewById(R.id.calendar_day_container);
        monthContainer = findViewById(R.id.calendar_month_container);
        dayTitle = findViewById(R.id.calendar_day_title);
        dayEmpty = findViewById(R.id.calendar_day_empty);
        previousDayButton = findViewById(R.id.calendar_day_previous);
        nextDayButton = findViewById(R.id.calendar_day_next);
        previousMonthButton = findViewById(R.id.calendar_month_previous);
        nextMonthButton = findViewById(R.id.calendar_month_next);
        monthTitle = findViewById(R.id.calendar_month_title);
        dayList = findViewById(R.id.calendar_day_list);
        monthGrid = findViewById(R.id.calendar_month_grid);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }
    private void configurarToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarToggle() {
        toggleGroup.check(R.id.calendar_toggle_day);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.calendar_toggle_day) {
                dayContainer.setVisibility(View.VISIBLE);
                monthContainer.setVisibility(View.GONE);
            } else {
                dayContainer.setVisibility(View.GONE);
                monthContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configurarListados() {
        dayAdapter = new CalendarDayAdapter(citasDelDia, this);
        dayList.setLayoutManager(new LinearLayoutManager(this));
        dayList.setAdapter(dayAdapter);

        monthAdapter = new CalendarMonthAdapter(this::mostrarCitasDelDia);
        monthGrid.setLayoutManager(new GridLayoutManager(this, 7));
        monthGrid.setAdapter(monthAdapter);

        previousDayButton.setOnClickListener(v -> moverDia(-1));
        nextDayButton.setOnClickListener(v -> moverDia(1));
        previousMonthButton.setOnClickListener(v -> moverMes(-1));
        nextMonthButton.setOnClickListener(v -> moverMes(1));
    }

    private void cargarCitas() {
        repository.loadAppointments(new CalendarRepository.LoadListener() {
            @Override
            public void onSuccess(@NonNull List<CalendarUiCita> citas) {
                runOnUiThread(() -> {
                    todasLasCitas.clear();
                    todasLasCitas.addAll(citas);
                    citasPorDia.clear();
                    for (CalendarUiCita cita : citas) {
                        agregarCitaADia(cita);
                    }
                    ordenarPorFechaGlobal();
                    actualizarDia();
                    actualizarMes();
                });
            }

            @Override
            public void onError(@NonNull Exception exception) {
                runOnUiThread(() -> Snackbar.make(root, R.string.calendario_error_cargar, Snackbar.LENGTH_LONG).show());
            }
        });
    }

    private void agregarCitaADia(@NonNull CalendarUiCita cita) {
        List<CalendarUiCita> citasDia = citasPorDia.get(cita.getFecha());
        if (citasDia == null) {
            citasDia = new ArrayList<>();
            citasPorDia.put(cita.getFecha(), citasDia);
        }
        if (!citasDia.contains(cita)) {
            citasDia.add(cita);
        }
        ordenarPorHora(citasDia);
    }

    private void removerCitaDeDia(@NonNull CalendarUiCita cita, @NonNull LocalDate fechaAnterior) {
        List<CalendarUiCita> citasDia = citasPorDia.get(fechaAnterior);
        if (citasDia != null) {
            citasDia.remove(cita);
            if (citasDia.isEmpty()) {
                citasPorDia.remove(fechaAnterior);
            }
        }
    }
    private void ordenarPorHora(@NonNull List<CalendarUiCita> citas) {
        Collections.sort(citas, (a, b) -> a.getHora().compareTo(b.getHora()));
    }

    private void moverDia(int offset) {
        fechaSeleccionada = fechaSeleccionada.plusDays(offset);
        mesActual = YearMonth.from(fechaSeleccionada);
        actualizarDia();
        actualizarMes();
    }
    private void moverMes(int offset) {
        mesActual = mesActual.plusMonths(offset);
        actualizarMes();
    }

    private void actualizarDia() {
        String titulo = capitalizar(fechaSeleccionada.format(dayTitleFormatter));
        dayTitle.setText(titulo);
        List<CalendarUiCita> citas = citasPorDia.getOrDefault(fechaSeleccionada, Collections.emptyList());
        citasDelDia.clear();
        citasDelDia.addAll(citas);
        dayAdapter.notifyDataSetChanged();
        dayEmpty.setVisibility(citasDelDia.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void actualizarMes() {
        String tituloMes = capitalizar(mesActual.format(monthTitleFormatter));
        monthTitle.setText(tituloMes);
        LocalDate primerDia = mesActual.atDay(1);
        int offset = primerDia.getDayOfWeek().getValue() % 7;
        LocalDate inicio = primerDia.minusDays(offset);
        List<CalendarMonthAdapter.CalendarMonthDay> dias = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            LocalDate fecha = inicio.plusDays(i);
            boolean esMesActual = fecha.getMonth() == mesActual.getMonth();
            boolean esHoy = fecha.equals(LocalDate.now());
            boolean esSeleccionado = fecha.equals(fechaSeleccionada);
            int cantidad = citasPorDia.containsKey(fecha) ? citasPorDia.get(fecha).size() : 0;
            dias.add(new CalendarMonthAdapter.CalendarMonthDay(fecha, esMesActual, esHoy, esSeleccionado, cantidad));
        }
        monthAdapter.updateDays(dias);
    }
    private String capitalizar(String texto) {
        if (TextUtils.isEmpty(texto)) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(locale) + texto.substring(1);
    }

    private void mostrarCitasDelDia(@NonNull LocalDate fecha) {
        fechaSeleccionada = fecha;
        actualizarDia();
        actualizarMes();
        List<CalendarUiCita> citas = citasPorDia.getOrDefault(fecha, Collections.emptyList());
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.sheet_calendar_day, null, false);
        dialog.setContentView(view);

        MaterialTextView titulo = view.findViewById(R.id.calendar_sheet_title);
        titulo.setText(capitalizar(fecha.format(dayTitleFormatter)));

        androidx.recyclerview.widget.RecyclerView recyclerView = view.findViewById(R.id.calendar_sheet_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CalendarDayAdapter adapter = new CalendarDayAdapter(new ArrayList<>(citas), seleccionada -> {
            dialog.dismiss();
            mostrarDialogoDetalle(seleccionada);
        });
        recyclerView.setAdapter(adapter);

        MaterialTextView empty = view.findViewById(R.id.calendar_sheet_empty);
        empty.setVisibility(citas.isEmpty() ? View.VISIBLE : View.GONE);

        dialog.show();
    }

    @Override
    public void onCitaClick(@NonNull CalendarUiCita cita) {
        mostrarDialogoDetalle(cita);
    }

    private void mostrarDialogoDetalle(@NonNull CalendarUiCita cita) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cita_detalle, null, false);
        MaterialTextView titulo = view.findViewById(R.id.calendario_detalle_titulo);
        MaterialTextView horario = view.findViewById(R.id.calendario_detalle_horario);
        MaterialTextView descripcion = view.findViewById(R.id.calendario_detalle_descripcion);
        MaterialSwitch notificarSwitch = view.findViewById(R.id.calendario_detalle_switch_notificar);
        MaterialButton reagendarButton = view.findViewById(R.id.calendario_detalle_btn_reagendar);
        MaterialButton eliminarButton = view.findViewById(R.id.calendario_detalle_btn_eliminar);

        String nombreActividad = cita.getActividadNombre();
        if (TextUtils.isEmpty(nombreActividad)) {
            nombreActividad = getString(R.string.calendario_evento_sin_actividad);
        }
        titulo.setText(nombreActividad);
        horario.setText(getString(R.string.calendario_detalle_horario,
                capitalizar(cita.getFecha().format(dateDetailFormatter)),
                cita.getHora().format(timeFormatter)));
        descripcion.setText(getString(R.string.calendario_detalle_lugar, cita.getLugar().getNombre()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        String finalNombreActividad = nombreActividad;
        reagendarButton.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogoReagendar(cita, finalNombreActividad, notificarSwitch.isChecked());
        });
        eliminarButton.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogoEliminar(cita, finalNombreActividad, notificarSwitch.isChecked());
        });

        dialog.show();
    }

    private void mostrarDialogoReagendar(@NonNull CalendarUiCita cita,
                                         @NonNull String nombreActividad,
                                         boolean notificar) {
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
            mostrarSelectorHora(cita, nombreActividad, nuevaFecha, notificar);
        });
        datePicker.show(getSupportFragmentManager(), "calendario_reagendar_fecha");
    }

    private void mostrarSelectorHora(@NonNull CalendarUiCita cita,
                                     @NonNull String nombreActividad,
                                     @NonNull LocalDate nuevaFecha,
                                     boolean notificar) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cita.getHora().getHour())
                .setMinute(cita.getHora().getMinute())
                .setTitleText(R.string.calendario_dialogo_reagendar_hora)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            LocalTime nuevaHora = LocalTime.of(picker.getHour(), picker.getMinute());
            repository.reagendar(cita, nuevaFecha, nuevaHora, new CalendarRepository.UpdateListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        LocalDate fechaAnterior = cita.getFecha();
                        removerCitaDeDia(cita, fechaAnterior);
                        cita.actualizarFechaHora(nuevaFecha, nuevaHora);
                        agregarCitaADia(cita);
                        ordenarPorFechaGlobal();
                        actualizarDia();
                        actualizarMes();
                        Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada,
                                nombreActividad,
                                capitalizar(cita.getFecha().format(dateDetailFormatter)),
                                cita.getHora().format(timeFormatter)), Snackbar.LENGTH_LONG).show();
                        if (notificar) {
                            Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(@NonNull Exception exception) {
                    runOnUiThread(() -> Snackbar.make(root, R.string.calendario_error_reagendar, Snackbar.LENGTH_LONG).show());
                }
            });
        });
        picker.show(getSupportFragmentManager(), "calendario_reagendar_hora");
    }
    private void mostrarDialogoEliminar(@NonNull CalendarUiCita cita,
                                        @NonNull String nombreActividad,
                                        boolean notificar) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.calendario_dialogo_eliminar_titulo)
                .setMessage(getString(R.string.calendario_dialogo_eliminar_mensaje, nombreActividad))
                .setPositiveButton(R.string.calendario_dialogo_eliminar_confirmar, (dialog, which) ->
                        repository.cancelar(cita, new CalendarRepository.UpdateListener() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    removerCitaDeDia(cita, cita.getFecha());
                                    todasLasCitas.remove(cita);
                                    actualizarDia();
                                    actualizarMes();
                                    Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, nombreActividad), Snackbar.LENGTH_LONG).show();
                                    if (notificar) {
                                        Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(@NonNull Exception exception) {
                                runOnUiThread(() -> Snackbar.make(root, R.string.calendario_error_cancelar, Snackbar.LENGTH_LONG).show());
                            }
                        }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void ordenarPorFechaGlobal() {
        Collections.sort(todasLasCitas, (a, b) -> {
            int compareFecha = a.getFecha().compareTo(b.getFecha());
            if (compareFecha != 0) {
                return compareFecha;
            }
            return a.getHora().compareTo(b.getHora());
        });
    }
}