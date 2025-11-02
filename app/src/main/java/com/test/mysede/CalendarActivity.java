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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity implements
        CalendarWeekAdapter.OnEventInteractionListener,
        CalendarMonthAdapter.OnDayClickListener {

    public static final String EXTRA_INITIAL_MODE = "com.test.mysede.calendar.EXTRA_INITIAL_MODE";
    public static final String MODE_WEEK = "WEEK";
    public static final String MODE_MONTH = "MONTH";

    private static final String COLLECTION_CITAS = "citas";
    private static final String COLLECTION_ACTIVIDADES = "actividades";

    private enum ViewMode { WEEK, MONTH }

    private final Locale locale = new Locale("es", "ES");
    private final DateTimeFormatter dayTitleFormatter = DateTimeFormatter.ofPattern("EEE d", locale);
    private final DateTimeFormatter dateDetailFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale);

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private CoordinatorLayout root;
    private MaterialButtonToggleGroup viewToggle;
    private View weekContainer;
    private View monthContainer;
    private MaterialTextView weekTitle;
    private RecyclerView weekRecycler;
    private MaterialButton previousWeekButton;
    private MaterialButton nextWeekButton;
    private MaterialButton previousMonthButton;
    private MaterialButton nextMonthButton;
    private MaterialTextView monthTitle;
    private RecyclerView monthRecycler;

    private final List<CalendarUiCita> allAppointments = new ArrayList<>();
    private final List<DaySchedule> currentWeekDays = new ArrayList<>();
    private final Map<LocalDate, List<CalendarUiCita>> citasPorFecha = new HashMap<>();

    private CalendarWeekAdapter weekAdapter;
    private CalendarMonthAdapter monthAdapter;

    private LocalDate selectedDate = LocalDate.now();
    private LocalDate currentWeekStart = selectedDate.with(DayOfWeek.MONDAY);
    private YearMonth currentMonth = YearMonth.from(selectedDate);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        root = findViewById(R.id.calendar_root);
        viewToggle = findViewById(R.id.calendar_view_toggle);
        weekContainer = findViewById(R.id.calendar_week_container);
        monthContainer = findViewById(R.id.calendar_month_container);
        weekTitle = findViewById(R.id.calendar_week_title);
        weekRecycler = findViewById(R.id.calendar_week_recycler);
        previousWeekButton = findViewById(R.id.calendar_week_previous);
        nextWeekButton = findViewById(R.id.calendar_week_next);
        previousMonthButton = findViewById(R.id.calendar_month_previous);
        nextMonthButton = findViewById(R.id.calendar_month_next);
        monthTitle = findViewById(R.id.calendar_month_title);
        monthRecycler = findViewById(R.id.calendar_month_recycler);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        weekAdapter = new CalendarWeekAdapter(currentWeekDays, dayTitleFormatter, this);
        weekRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        weekRecycler.setAdapter(weekAdapter);

        monthAdapter = new CalendarMonthAdapter(this);
        monthRecycler.setLayoutManager(new GridLayoutManager(this, 7));
        monthRecycler.setAdapter(monthAdapter);

        previousWeekButton.setOnClickListener(v -> navegarSemana(-1));
        nextWeekButton.setOnClickListener(v -> navegarSemana(1));
        previousMonthButton.setOnClickListener(v -> navegarMes(-1));
        nextMonthButton.setOnClickListener(v -> navegarMes(1));

        viewToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.calendar_toggle_week) {
                cambiarModo(ViewMode.WEEK);
            } else if (checkedId == R.id.calendar_toggle_month) {
                cambiarModo(ViewMode.MONTH);
            }
        });

        String initialMode = getIntent().getStringExtra(EXTRA_INITIAL_MODE);
        if (MODE_MONTH.equals(initialMode)) {
            viewToggle.check(R.id.calendar_toggle_month);
        } else {
            viewToggle.check(R.id.calendar_toggle_week);
        }

        actualizarSeleccion(selectedDate);
        cargarCitasDesdeFirestore();
    }

    private void cambiarModo(ViewMode mode) {
        weekContainer.setVisibility(mode == ViewMode.WEEK ? View.VISIBLE : View.GONE);
        monthContainer.setVisibility(mode == ViewMode.MONTH ? View.VISIBLE : View.GONE);
        if (mode == ViewMode.WEEK) {
            refrescarSemana();
        } else {
            refrescarMes();
        }
    }

    private void navegarSemana(int offset) {
        LocalDate referencia = selectedDate.plusWeeks(offset);
        actualizarSeleccion(referencia);
    }

    private void navegarMes(int offset) {
        currentMonth = currentMonth.plusMonths(offset);
        int dia = Math.min(selectedDate.getDayOfMonth(), currentMonth.lengthOfMonth());
        LocalDate nuevaFecha = currentMonth.atDay(dia);
        actualizarSeleccion(nuevaFecha);
    }

    private void actualizarSeleccion(LocalDate fecha) {
        mostrarSemana(fecha);
        currentMonth = YearMonth.from(selectedDate);
        refrescarMes();
    }

    private void refrescarSemana() {
        mostrarSemana(selectedDate);
    }

    private void refrescarMes() {
        if (monthTitle == null || monthAdapter == null) return;

        LocalDate firstOfMonth = currentMonth.atDay(1);
        LocalDate firstDisplayed = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        List<CalendarMonthAdapter.MonthDay> days = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            LocalDate date = firstDisplayed.plusDays(i);
            boolean isCurrentMonth = YearMonth.from(date).equals(currentMonth);
            int count = citasPorFecha.containsKey(date) ? citasPorFecha.get(date).size() : 0;
            days.add(new CalendarMonthAdapter.MonthDay(date, isCurrentMonth, count));
        }
        monthTitle.setText(capitalizar(currentMonth.format(monthTitleFormatter)));
        monthAdapter.submitData(days, selectedDate);
    }

    private void mostrarSemana(LocalDate referenceDate) {
        selectedDate = referenceDate;
        currentWeekStart = referenceDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentWeekStart.plusDays(6);

        currentWeekDays.clear();
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            DaySchedule schedule = new DaySchedule(date);
            List<CalendarUiCita> citasDia = citasPorFecha.get(date);
            if (citasDia != null) {
                for (CalendarUiCita cita : citasDia) {
                    schedule.addAppointment(cita);
                }
            }
            currentWeekDays.add(schedule);
        }

        if (weekTitle != null) {
            String titleText = capitalizar(currentWeekStart.format(dateDetailFormatter))
                    + " - "
                    + capitalizar(endOfWeek.format(dateDetailFormatter));
            weekTitle.setText(titleText);
        }
        if (weekAdapter != null) {
            weekAdapter.notifyDataSetChanged();
        }
        if (weekRecycler != null && currentWeekDays.size() > 0) {
            int position = Math.min(Math.max(referenceDate.getDayOfWeek().getValue() - 1, 0), currentWeekDays.size() - 1);
            weekRecycler.scrollToPosition(position);
        }
        currentMonth = YearMonth.from(selectedDate);
    }

    private void cargarCitasDesdeFirestore() {
        firestore.collection(COLLECTION_CITAS)
                .get()
                .addOnSuccessListener(this::procesarCitas)
                .addOnFailureListener(e -> {
                    Snackbar.make(root, R.string.calendario_error_cargar_citas, Snackbar.LENGTH_LONG).show();
                    allAppointments.clear();
                    actualizarIndicePorFecha();
                    refrescarSemana();
                    refrescarMes();
                });
    }

    private void procesarCitas(QuerySnapshot snapshot) {
        List<RawCita> rawCitas = new ArrayList<>();
        Set<String> actividadIds = new HashSet<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String fechaStr = document.getString("fecha");
            String horaStr = document.getString("hora");
            if (fechaStr == null || horaStr == null) continue;

            try {
                LocalDate fecha = LocalDate.parse(fechaStr);
                LocalTime hora = LocalTime.parse(horaStr);
                String actividadId = document.getString("actividadId");
                String actividadNombre = document.getString("actividadNombre");
                Map<String, Object> lugarData = document.get("lugar", Map.class);
                String lugarNombre = null;
                if (lugarData != null) {
                    Object nombre = lugarData.get("nombre");
                    if (nombre instanceof String) {
                        lugarNombre = (String) nombre;
                    }
                }
                rawCitas.add(new RawCita(document.getId(), actividadId, actividadNombre, lugarNombre, fecha, hora));
                if (TextUtils.isEmpty(actividadNombre) && !TextUtils.isEmpty(actividadId)) {
                    actividadIds.add(actividadId);
                }
            } catch (Exception ignored) {}
        }

        if (actividadIds.isEmpty()) {
            construirCitas(rawCitas, new HashMap<>());
        } else {
            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String actividadId : actividadIds) {
                tasks.add(firestore.collection(COLLECTION_ACTIVIDADES).document(actividadId).get());
            }
            Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        Map<String, String> nombres = new HashMap<>();
                        for (Object result : results) {
                            if (result instanceof DocumentSnapshot) {
                                DocumentSnapshot document = (DocumentSnapshot) result;
                                if (document.exists()) {
                                    String nombre = document.getString("nombre");
                                    if (!TextUtils.isEmpty(nombre)) {
                                        nombres.put(document.getId(), nombre);
                                    }
                                }
                            }
                        }
                        construirCitas(rawCitas, nombres);
                    })
                    .addOnFailureListener(e -> construirCitas(rawCitas, new HashMap<>()));
        }
    }

    private void construirCitas(List<RawCita> rawCitas, Map<String, String> nombresActividades) {
        allAppointments.clear();
        for (RawCita raw : rawCitas) {
            String nombreActividad = !TextUtils.isEmpty(raw.actividadNombre)
                    ? raw.actividadNombre
                    : nombresActividades.get(raw.actividadId);
            if (TextUtils.isEmpty(nombreActividad)) {
                nombreActividad = raw.actividadId != null ? raw.actividadId : getString(R.string.calendario_actividad_sin_nombre);
            }
            String lugarNombre = !TextUtils.isEmpty(raw.lugarNombre)
                    ? raw.lugarNombre
                    : getString(R.string.calendario_lugar_desconocido);
            allAppointments.add(new CalendarUiCita(
                    raw.firestoreId,
                    raw.actividadId,
                    nombreActividad,
                    lugarNombre,
                    raw.fecha,
                    raw.hora,
                    60));
        }
        actualizarIndicePorFecha();
        refrescarSemana();
        refrescarMes();
    }

    private void actualizarIndicePorFecha() {
        citasPorFecha.clear();
        for (CalendarUiCita cita : allAppointments) {
            citasPorFecha.computeIfAbsent(cita.getFecha(), key -> new ArrayList<>()).add(cita);
        }
        for (List<CalendarUiCita> citas : citasPorFecha.values()) {
            citas.sort(Comparator.comparing(CalendarUiCita::getHora));
        }
    }

    private List<CalendarUiCita> obtenerCitasPorFecha(LocalDate fecha) {
        List<CalendarUiCita> citas = citasPorFecha.get(fecha);
        if (citas == null) return new ArrayList<>();
        return new ArrayList<>(citas);
    }

    private void mostrarCitasDelDia(LocalDate fecha) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_calendar_day, null, false);
        MaterialTextView titulo = view.findViewById(R.id.calendar_day_sheet_title);
        MaterialTextView subtitulo = view.findViewById(R.id.calendar_day_sheet_subtitle);
        RecyclerView lista = view.findViewById(R.id.calendar_day_sheet_list);
        MaterialTextView emptyView = view.findViewById(R.id.calendar_day_sheet_empty);

        titulo.setText(capitalizar(fecha.format(dateDetailFormatter)));
        List<CalendarUiCita> citas = obtenerCitasPorFecha(fecha);
        subtitulo.setText(getResources().getQuantityString(R.plurals.calendario_citas_count, citas.size(), citas.size()));

        lista.setLayoutManager(new LinearLayoutManager(this));
        CalendarDayAppointmentsAdapter adapter = new CalendarDayAppointmentsAdapter(cita -> {
            dialog.dismiss();
            mostrarDialogoDetalle(cita);
        });
        lista.setAdapter(adapter);
        adapter.submitList(citas);

        emptyView.setVisibility(citas.isEmpty() ? View.VISIBLE : View.GONE);

        dialog.setContentView(view);
        dialog.show();
    }

    private void reagendarCita(CalendarUiCita cita, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Objects.requireNonNull(cita, "La cita es obligatoria");
        Objects.requireNonNull(nuevaFecha, "La fecha es obligatoria");
        Objects.requireNonNull(nuevaHora, "La hora es obligatoria");

        String firestoreId = cita.getFirestoreId();
        if (TextUtils.isEmpty(firestoreId)) {
            cita.actualizarFechaHora(nuevaFecha, nuevaHora);
            actualizarIndicePorFecha();
            actualizarSeleccion(nuevaFecha);
            Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada,
                    cita.getActividadNombre(),
                    capitalizar(nuevaFecha.format(dateDetailFormatter)),
                    nuevaHora.format(timeFormatter)), Snackbar.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fecha", nuevaFecha.format(DateTimeFormatter.ISO_LOCAL_DATE));
        updates.put("hora", nuevaHora.format(DateTimeFormatter.ISO_LOCAL_TIME));

        firestore.collection(COLLECTION_CITAS)
                .document(firestoreId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    cita.actualizarFechaHora(nuevaFecha, nuevaHora);
                    actualizarIndicePorFecha();
                    actualizarSeleccion(nuevaFecha);
                    Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada,
                            cita.getActividadNombre(),
                            capitalizar(nuevaFecha.format(dateDetailFormatter)),
                            nuevaHora.format(timeFormatter)), Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(root, R.string.calendario_error_actualizar_cita, Snackbar.LENGTH_LONG).show());
    }

    private void eliminarCita(CalendarUiCita cita) {
        String firestoreId = cita.getFirestoreId();
        if (TextUtils.isEmpty(firestoreId)) {
            allAppointments.remove(cita);
            actualizarIndicePorFecha();
            refrescarSemana();
            refrescarMes();
            Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, cita.getActividadNombre()), Snackbar.LENGTH_LONG).show();
            return;
        }
        firestore.collection(COLLECTION_CITAS)
                .document(firestoreId)
                .delete()
                .addOnSuccessListener(unused -> {
                    allAppointments.remove(cita);
                    actualizarIndicePorFecha();
                    refrescarSemana();
                    refrescarMes();
                    Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, cita.getActividadNombre()), Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(root, R.string.calendario_error_eliminar_cita, Snackbar.LENGTH_LONG).show());
    }

    private void mostrarDialogoDetalle(CalendarUiCita cita) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cita_detalle, null, false);
        MaterialTextView titulo = view.findViewById(R.id.calendario_detalle_titulo);
        MaterialTextView horario = view.findViewById(R.id.calendario_detalle_horario);
        MaterialTextView descripcion = view.findViewById(R.id.calendario_detalle_descripcion);
        MaterialSwitch notificarSwitch = view.findViewById(R.id.calendario_detalle_switch_notificar);
        MaterialButton reagendarButton = view.findViewById(R.id.calendario_detalle_btn_reagendar);
        MaterialButton eliminarButton = view.findViewById(R.id.calendario_detalle_btn_eliminar);

        titulo.setText(cita.getActividadNombre());
        horario.setText(getString(R.string.calendario_detalle_horario,
                capitalizar(cita.getFecha().format(dateDetailFormatter)),
                cita.getHora().format(timeFormatter)));
        descripcion.setText(getString(R.string.calendario_detalle_lugar, cita.getLugarNombre()));

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
            if (selection == null) return;
            LocalDate nuevaFecha = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate();
            mostrarSelectorHora(cita, nuevaFecha, notificar);
        });
        datePicker.show(getSupportFragmentManager(), "calendario_reagendar_fecha");
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
                .setMessage(getString(R.string.calendario_dialogo_eliminar_mensaje, cita.getActividadNombre()))
                .setPositiveButton(R.string.calendario_dialogo_eliminar_confirmar, (dialog, which) -> {
                    eliminarCita(cita);
                    if (notificar) {
                        Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String capitalizar(String texto) {
        if (TextUtils.isEmpty(texto)) return texto;
        return texto.substring(0, 1).toUpperCase(locale) + texto.substring(1);
    }

    @Override
    public void onEventSelected(@NonNull CalendarUiCita cita) {
        mostrarDialogoDetalle(cita);
    }

    @Override
    public void onEventDropped(@NonNull CalendarUiCita cita, @NonNull LocalDate nuevaFecha) {
        reagendarCita(cita, nuevaFecha, cita.getHora());
    }

    @Override
    public void onDayClicked(@NonNull LocalDate date) {
        actualizarSeleccion(date);
        mostrarCitasDelDia(date);
    }

    private static final class RawCita {
        final String firestoreId;
        final String actividadId;
        final String actividadNombre;
        final String lugarNombre;
        final LocalDate fecha;
        final LocalTime hora;

        RawCita(String firestoreId, String actividadId, String actividadNombre, String lugarNombre, LocalDate fecha, LocalTime hora) {
            this.firestoreId = firestoreId;
            this.actividadId = actividadId;
            this.actividadNombre = actividadNombre;
            this.lugarNombre = lugarNombre;
            this.fecha = fecha;
            this.hora = hora;
        }
    }
}
