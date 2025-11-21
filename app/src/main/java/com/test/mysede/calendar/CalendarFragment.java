package com.test.mysede.calendar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.time.ZoneOffset;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.test.mysede.model.Periodicidad;
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
import com.test.mysede.calendar.CalendarDayAppointmentsAdapter;
import com.test.mysede.calendar.CalendarMonthAdapter;
import com.test.mysede.calendar.CalendarUiCita;
import com.test.mysede.calendar.CalendarWeekAdapter;
import com.test.mysede.DAO.ActividadDAO;
import com.test.mysede.DAO.CitaDAO;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.calendar.DaySchedule;
import com.test.mysede.R;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;
import android.app.Activity;

/**
 * Fragment que muestra el calendario de actividades/citas.
 */
public class CalendarFragment extends Fragment implements
        CalendarWeekAdapter.OnEventInteractionListener,
        CalendarMonthAdapter.OnDayClickListener {

    public static final String ARG_INITIAL_MODE = "com.test.mysede.calendar.ARG_INITIAL_MODE";
    public static final String MODE_WEEK = "WEEK";
    public static final String MODE_MONTH = "MONTH";

    private enum ViewMode { WEEK, MONTH }

    public static CalendarFragment newInstance(@Nullable String initialMode) {
        CalendarFragment fragment = new CalendarFragment();
        if (initialMode != null) {
            Bundle args = new Bundle();
            args.putString(ARG_INITIAL_MODE, initialMode);
            fragment.setArguments(args);
        }
        return fragment;
    }

    private final Locale locale = new Locale("es", "ES");
    private final DateTimeFormatter dayTitleFormatter = DateTimeFormatter.ofPattern("EEE d", locale);
    private final DateTimeFormatter dateDetailFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale);

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final CitaDAO citaDAO = new CitaDAO();

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

    // ============================================
    // NUEVA VARIABLE PARA CONTROLAR ESTADO DEL FRAGMENT
    // ============================================
    private boolean isFragmentActive = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        root = view.findViewById(R.id.calendar_root);
        viewToggle = view.findViewById(R.id.calendar_view_toggle);
        weekContainer = view.findViewById(R.id.calendar_week_container);
        monthContainer = view.findViewById(R.id.calendar_month_container);
        weekTitle = view.findViewById(R.id.calendar_week_title);
        weekRecycler = view.findViewById(R.id.calendar_week_recycler);
        previousWeekButton = view.findViewById(R.id.calendar_week_previous);
        nextWeekButton = view.findViewById(R.id.calendar_week_next);
        previousMonthButton = view.findViewById(R.id.calendar_month_previous);
        nextMonthButton = view.findViewById(R.id.calendar_month_next);
        monthTitle = view.findViewById(R.id.calendar_month_title);
        monthRecycler = view.findViewById(R.id.calendar_month_recycler);

        weekAdapter = new CalendarWeekAdapter(currentWeekDays, dayTitleFormatter, this);
        weekRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        weekRecycler.setAdapter(weekAdapter);

        monthAdapter = new CalendarMonthAdapter(this);
        monthRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 7));
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

        String initialMode = null;
        Bundle args = getArguments();
        if (args != null) {
            initialMode = args.getString(ARG_INITIAL_MODE);
        }
        if (MODE_MONTH.equals(initialMode)) {
            viewToggle.check(R.id.calendar_toggle_month);
        } else {
            viewToggle.check(R.id.calendar_toggle_week);
        }

        actualizarSeleccion(selectedDate);
        cargarCitasDesdeDao();
    }

    // ============================================
    // CONTROL DE CICLO DE VIDA DEL FRAGMENT
    // ============================================
    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive = false;
    }

    // ============================================
    // MÉTODO AUXILIAR PARA EJECUTAR EN UI THREAD DE FORMA SEGURA
    // ============================================
    private void runOnUiThreadSafely(Runnable action) {
        if (!isFragmentActive || !isAdded() || getActivity() == null) {
            return;
        }

        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if (isFragmentActive && isAdded() && getActivity() != null) {
                    action.run();
                }
            });
        }
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

    // ============================================
    // MÉTODO CORREGIDO: cargarCitasDesdeDao
    // ============================================
    private void cargarCitasDesdeDao() {
        actividadDAO.getAllActividades(new ActividadDAO.OnActividadesLoadedListener() {
            @Override
            public void onActividadesLoaded(ArrayList<Actividad> actividades) {
                // Validar si el Fragment aún está adjunto
                if (!isFragmentActive || !isAdded() || getActivity() == null) {
                    return;
                }

                if (actividades == null || actividades.isEmpty()) {
                    runOnUiThreadSafely(() -> {
                        allAppointments.clear();
                        actualizarIndicePorFecha();
                        refrescarSemana();
                        refrescarMes();
                    });
                    return;
                }

                final AtomicInteger pendientes = new AtomicInteger(actividades.size());
                final AtomicBoolean errorReportado = new AtomicBoolean(false);
                final List<CalendarUiCita> citasRecolectadas = Collections.synchronizedList(new ArrayList<>());

                for (Actividad actividad : actividades) {
                    citaDAO.getCitasPorActividad(actividad, new CitaDAO.OnCitasLoadedListener() {
                        @Override
                        public void onCitasLoaded(ArrayList<Cita> citas) {
                            // Validar si el Fragment aún está adjunto
                            if (!isFragmentActive || !isAdded() || getActivity() == null) {
                                return;
                            }

                            if (citas != null) {
                                for (Cita cita : citas) {
                                    citasRecolectadas.add(CalendarUiCita.fromCita(cita));
                                }
                            }
                            verificarFinalizacion();
                        }

                        @Override
                        public void onError(Exception e) {
                            // Validar si el Fragment aún está adjunto
                            if (!isFragmentActive || !isAdded() || getActivity() == null) {
                                return;
                            }

                            if (errorReportado.compareAndSet(false, true)) {
                                runOnUiThreadSafely(() -> {
                                    if (root != null) {
                                        Snackbar.make(root, R.string.calendario_error_cargar_citas, Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                            verificarFinalizacion();
                        }

                        private void verificarFinalizacion() {
                            // Validar si el Fragment aún está adjunto
                            if (!isFragmentActive || !isAdded() || getActivity() == null) {
                                return;
                            }

                            if (pendientes.decrementAndGet() == 0) {
                                List<CalendarUiCita> snapshot;
                                synchronized (citasRecolectadas) {
                                    snapshot = new ArrayList<>(citasRecolectadas);
                                }
                                runOnUiThreadSafely(() -> actualizarCalendario(snapshot));
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThreadSafely(() -> {
                    if (root != null) {
                        Snackbar.make(root, R.string.calendario_error_cargar_citas, Snackbar.LENGTH_LONG).show();
                    }
                    allAppointments.clear();
                    actualizarIndicePorFecha();
                    refrescarSemana();
                    refrescarMes();
                });
            }
        });
    }

    private void actualizarCalendario(List<CalendarUiCita> nuevasCitas) {
        allAppointments.clear();
        if (nuevasCitas != null) {
            allAppointments.addAll(nuevasCitas);
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
        if (!isAdded() || getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_calendar_day, null, false);
        MaterialTextView titulo = dialogView.findViewById(R.id.calendar_day_sheet_title);
        MaterialTextView subtitulo = dialogView.findViewById(R.id.calendar_day_sheet_subtitle);
        RecyclerView lista = dialogView.findViewById(R.id.calendar_day_sheet_list);
        MaterialTextView emptyView = dialogView.findViewById(R.id.calendar_day_sheet_empty);

        titulo.setText(capitalizar(fecha.format(dateDetailFormatter)));
        List<CalendarUiCita> citas = obtenerCitasPorFecha(fecha);
        subtitulo.setText(getResources().getQuantityString(R.plurals.calendario_citas_count, citas.size(), citas.size()));

        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        CalendarDayAppointmentsAdapter adapter = new CalendarDayAppointmentsAdapter(cita -> {
            dialog.dismiss();
            mostrarDialogoDetalle(cita);
        });
        lista.setAdapter(adapter);
        adapter.submitList(citas);

        emptyView.setVisibility(citas.isEmpty() ? View.VISIBLE : View.GONE);

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private boolean perteneceActividad(CalendarUiCita uiCita, Actividad actividad) {
        if (uiCita == null || actividad == null) {
            return false;
        }
        String actividadId = actividad.getId();
        if (!TextUtils.isEmpty(actividadId)) {
            return actividadId.equals(uiCita.getActividadId());
        }
        Actividad citaActividad = uiCita.getActividad();
        return citaActividad != null && citaActividad == actividad;
    }

    private void ajustarPeriodicidadActividad(@Nullable Actividad actividad) {
        if (actividad == null) {
            return;
        }

        Periodicidad periodicidadActual = actividad.getPeriodicidad();
        if (periodicidadActual == null) {
            return;
        }

        List<LocalDate> fechasActividad = new ArrayList<>();
        for (CalendarUiCita uiCita : allAppointments) {
            if (perteneceActividad(uiCita, actividad)) {
                fechasActividad.add(uiCita.getFecha());
            }
        }

        if (fechasActividad.isEmpty()) {
            return;
        }

        LocalDate nuevaFechaInicio = Collections.min(fechasActividad);
        LocalDate nuevaFechaFin = Collections.max(fechasActividad);

        LocalDate inicioActual = periodicidadActual.getFechaInicio().orElse(null);
        LocalDate finActual = periodicidadActual.getFechaFin().orElse(null);

        Periodicidad nuevaPeriodicidad;
        if (periodicidadActual.getTipo() == Periodicidad.Tipo.PUNTUAL) {
            if (Objects.equals(inicioActual, nuevaFechaInicio)) {
                return;
            }
            nuevaPeriodicidad = Periodicidad.puntual(periodicidadActual.getNombre(), nuevaFechaInicio);
        } else {
            if (Objects.equals(inicioActual, nuevaFechaInicio) && Objects.equals(finActual, nuevaFechaFin)) {
                return;
            }
            nuevaPeriodicidad = Periodicidad.periodica(periodicidadActual.getNombre(), nuevaFechaInicio, nuevaFechaFin);
        }

        actividad.setPeriodicidad(nuevaPeriodicidad);
        if (!TextUtils.isEmpty(actividad.getId())) {
            actividadDAO.updateActividad(actividad, null);
        }
    }

    private void reagendarCita(CalendarUiCita cita, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Objects.requireNonNull(cita, "La cita es obligatoria");
        Objects.requireNonNull(nuevaFecha, "La fecha es obligatoria");
        Objects.requireNonNull(nuevaHora, "La hora es obligatoria");

        if (!PermissionManager.tienePermiso(Permiso.EDITAR_CITA)) {
            if (root != null) {
                Snackbar.make(root, "No tienes permiso para reagendar citas", Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        if (!nuevaFecha.isAfter(LocalDate.now())) {
            if (root != null) {
                Snackbar.make(root, R.string.calendario_error_fecha_pasada, Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        String firestoreId = cita.getFirestoreId();
        if (TextUtils.isEmpty(firestoreId)) {
            cita.actualizarFechaHora(nuevaFecha, nuevaHora);
            actualizarIndicePorFecha();
            actualizarSeleccion(nuevaFecha);
            if (root != null) {
                Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada,
                        cita.getActividadNombre(),
                        capitalizar(nuevaFecha.format(dateDetailFormatter)),
                        nuevaHora.format(timeFormatter)), Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        Cita citaActualizada = cita.crearModelo(nuevaFecha, nuevaHora);
        if (citaActualizada == null) {
            cita.actualizarFechaHora(nuevaFecha, nuevaHora);
            actualizarIndicePorFecha();
            actualizarSeleccion(nuevaFecha);
            if (root != null) {
                Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada,
                        cita.getActividadNombre(),
                        capitalizar(nuevaFecha.format(dateDetailFormatter)),
                        nuevaHora.format(timeFormatter)), Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        citaDAO.saveCita(citaActualizada, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThreadSafely(() -> {
                    cita.actualizarModelo(citaActualizada);
                    actualizarCitaEnActividad(citaActualizada);
                    actualizarIndicePorFecha();
                    actualizarSeleccion(nuevaFecha);
                    ajustarPeriodicidadActividad(cita.getActividad());
                    if (root != null) {
                        Snackbar.make(root, getString(R.string.calendario_snackbar_reagendada,
                                cita.getActividadNombre(),
                                capitalizar(nuevaFecha.format(dateDetailFormatter)),
                                nuevaHora.format(timeFormatter)), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                runOnUiThreadSafely(() -> {
                    if (root != null) {
                        Snackbar.make(root, R.string.calendario_error_actualizar_cita, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void eliminarCita(CalendarUiCita cita) {
        if (!PermissionManager.tienePermiso(Permiso.ELIMINAR_CITA)) {
            if (root != null) {
                Snackbar.make(root, "No tienes permiso para eliminar citas", Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        String firestoreId = cita.getFirestoreId();
        if (TextUtils.isEmpty(firestoreId)) {
            allAppointments.remove(cita);
            actualizarIndicePorFecha();
            refrescarSemana();
            refrescarMes();
            ajustarPeriodicidadActividad(cita.getActividad());
            if (root != null) {
                Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, cita.getActividadNombre()), Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        Cita modelo = cita.getCita();
        if (modelo == null) {
            modelo = cita.crearModelo(cita.getFecha(), cita.getHora());
        }
        if (modelo == null) {
            allAppointments.remove(cita);
            actualizarIndicePorFecha();
            refrescarSemana();
            refrescarMes();
            ajustarPeriodicidadActividad(cita.getActividad());
            if (root != null) {
                Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, cita.getActividadNombre()), Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        Cita finalModelo = modelo;
        citaDAO.deleteCita(finalModelo, new FirestoreOperationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThreadSafely(() -> {
                    allAppointments.remove(cita);
                    eliminarCitaDeActividad(cita);
                    actualizarIndicePorFecha();
                    refrescarSemana();
                    refrescarMes();
                    ajustarPeriodicidadActividad(cita.getActividad());
                    if (root != null) {
                        Snackbar.make(root, getString(R.string.calendario_snackbar_eliminada, cita.getActividadNombre()), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                runOnUiThreadSafely(() -> {
                    if (root != null) {
                        Snackbar.make(root, R.string.calendario_error_eliminar_cita, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void mostrarDialogoDetalle(CalendarUiCita cita) {
        if (!isAdded() || getContext() == null) return;

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cita_detalle, null, false);
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

        final AlertDialog[] dialogHolder = new AlertDialog[1];

        boolean puedeEditar = PermissionManager.tienePermiso(Permiso.EDITAR_CITA);
        boolean puedeEliminar = PermissionManager.tienePermiso(Permiso.ELIMINAR_CITA);
        boolean puedeRecibirNotificaciones = PermissionManager.tienePermiso(Permiso.RECIBIR_NOTIFICACIONES);

        if (!puedeRecibirNotificaciones || (!puedeEditar && !puedeEliminar)) {
            notificarSwitch.setVisibility(View.GONE);
        }

        if (puedeEditar) {
            reagendarButton.setVisibility(View.VISIBLE);
            reagendarButton.setOnClickListener(v -> {
                if (dialogHolder[0] != null) {
                    dialogHolder[0].dismiss();
                }
                mostrarDialogoReagendar(cita, notificarSwitch.isChecked());
            });
        } else {
            reagendarButton.setVisibility(View.GONE);
        }

        if (puedeEliminar) {
            eliminarButton.setVisibility(View.VISIBLE);
            eliminarButton.setOnClickListener(v -> {
                if (dialogHolder[0] != null) {
                    dialogHolder[0].dismiss();
                }
                mostrarDialogoEliminar(cita, notificarSwitch.isChecked());
            });
        } else {
            eliminarButton.setVisibility(View.GONE);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .create();

        dialogHolder[0] = dialog;
        dialog.show();
    }

    private void mostrarDialogoReagendar(CalendarUiCita cita, boolean notificar) {
        if (!isAdded()) return;

        long seleccionInicial = cita.getFecha()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.calendario_dialogo_reagendar_fecha)
                .setSelection(seleccionInicial)
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (!isAdded() || selection == null) return;

            LocalDate nuevaFecha = Instant.ofEpochMilli(selection)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            if (!nuevaFecha.isAfter(LocalDate.now())) {
                if (root != null) {
                    Snackbar.make(root, R.string.calendario_error_fecha_pasada, Snackbar.LENGTH_LONG).show();
                }
                return;
            }

            mostrarSelectorHora(cita, nuevaFecha, notificar);
        });

        if (getParentFragmentManager() != null) {
            datePicker.show(getParentFragmentManager(), "calendario_reagendar_fecha");
        }
    }

    private void mostrarSelectorHora(CalendarUiCita cita, LocalDate nuevaFecha, boolean notificar) {
        if (!isAdded()) return;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cita.getHora().getHour())
                .setMinute(cita.getHora().getMinute())
                .setTitleText(R.string.calendario_dialogo_reagendar_hora)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            if (!isAdded()) return;

            LocalTime nuevaHora = LocalTime.of(picker.getHour(), picker.getMinute());
            reagendarCita(cita, nuevaFecha, nuevaHora);
            if (notificar && root != null) {
                Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
            }
        });

        if (getParentFragmentManager() != null) {
            picker.show(getParentFragmentManager(), "calendario_reagendar_hora");
        }
    }

    private void mostrarDialogoEliminar(CalendarUiCita cita, boolean notificar) {
        if (!isAdded() || getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.calendario_dialogo_eliminar_titulo)
                .setMessage(getString(R.string.calendario_dialogo_eliminar_mensaje, cita.getActividadNombre()))
                .setPositiveButton(R.string.calendario_dialogo_eliminar_confirmar, (dialog, which) -> {
                    eliminarCita(cita);
                    if (notificar && root != null) {
                        Snackbar.make(root, R.string.calendario_snackbar_notificacion, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void actualizarCitaEnActividad(@Nullable Cita citaActualizada) {
        if (citaActualizada == null) return;
        Actividad actividad = citaActualizada.getActividad();
        if (actividad == null || TextUtils.isEmpty(actividad.getId())) return;
        actividad.agregarOCambiarCita(citaActualizada);
        actividadDAO.saveActividad(actividad);
    }

    private void eliminarCitaDeActividad(@NonNull CalendarUiCita cita) {
        Actividad actividad = cita.getActividad();
        String citaId = cita.getFirestoreId();
        if (actividad == null || TextUtils.isEmpty(actividad.getId()) || TextUtils.isEmpty(citaId)) return;
        actividad.eliminarCitaPorId(citaId);
        actividadDAO.saveActividad(actividad);
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
}