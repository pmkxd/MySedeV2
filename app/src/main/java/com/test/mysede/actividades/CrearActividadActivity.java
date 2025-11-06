package com.test.mysede.actividades;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.test.mysede.AdjuntarArchivosActivity;
import com.test.mysede.ArchivoAdjunto;
import com.test.mysede.DAO.ActividadDAO;
import com.test.mysede.DAO.ArchivoAdjuntoDAO;
import com.test.mysede.DAO.CitaDAO;
import com.test.mysede.DAO.FirestoreOperationCallback;
import com.test.mysede.DAO.LugarDAO;
import com.test.mysede.DAO.OferenteActividadDAO;
import com.test.mysede.DAO.ProyectoDAO;
import com.test.mysede.DAO.SocioComunitarioDAO;
import com.test.mysede.DAO.TipoActividadDAO;
import com.test.mysede.R;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.Periodicidad;
import com.test.mysede.model.Proyecto;
import com.test.mysede.model.SocioComunitario;
import com.test.mysede.model.TipoActividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CrearActividadActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etCupo, etDiasAviso;
    private TextInputEditText etFechaPuntual, etHoraPuntual, etFechaInicio, etHoraPeriodica, etRepeticiones;
    private Spinner spinnerTipo, spinnerProyecto, spinnerOferente, spinnerSocio, spinnerLugar;
    private ChipGroup chipGroupDias;
    private RadioGroup rgPeriodicidad;
    private RadioButton rbPuntual;
    private RadioButton rbPeriodica;

    private Button btnGuardar;
    private MaterialButton btnAdjuntarArchivo;
    private LinearLayout layoutArchivosAdjuntos;
    private View layoutFechaPuntual, layoutFechasPeriodicas;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final TipoActividadDAO tipoActividadDAO = new TipoActividadDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final OferenteActividadDAO oferenteDAO = new OferenteActividadDAO();
    private final SocioComunitarioDAO socioDAO = new SocioComunitarioDAO();
    private final LugarDAO lugarDAO = new LugarDAO();
    private final CitaDAO citaDAO = new CitaDAO();

    private final List<TipoActividad> tiposActividad = new ArrayList<>();
    private final List<Proyecto> proyectos = new ArrayList<>();
    private final List<OferenteActividad> oferentes = new ArrayList<>();
    private final List<SocioComunitario> socios = new ArrayList<>();
    private final List<Lugar> lugares = new ArrayList<>();

    private ArrayList<ArchivoAdjunto> archivosAdjuntos = new ArrayList<>();
    private final ArchivoAdjuntoDAO archivoDAO = new ArchivoAdjuntoDAO();



    private boolean modoEditar = false;
    private String actividadId;
    private Actividad actividadEditar;

    private LocalDate fechaPuntualSeleccionada;
    private LocalDate fechaInicioSeleccionada;
    private LocalTime horaPuntualSeleccionada;
    private LocalTime horaPeriodicaSeleccionada;

    private boolean tiposCargados;
    private boolean proyectosCargados;
    private boolean oferentesCargados;
    private boolean sociosCargados;
    private boolean lugaresCargados;
    private boolean actividadCargada;
    private boolean citasCargadas;


    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "CL"));
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        modoEditar = "editar".equals(getIntent().getStringExtra("modo"));
        actividadId = getIntent().getStringExtra("actividadId");
        if (modoEditar && actividadId == null) {
            Toast.makeText(this, "No se encontró la actividad a editar", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(modoEditar ? "Editar Actividad" : "Crear Actividad");
        }

        inicializarVistas();
        configurarEventos();

        btnAdjuntarArchivo = findViewById(R.id.btnAdjuntarArchivo);
        layoutArchivosAdjuntos = findViewById(R.id.layoutArchivosAdjuntos);
        btnAdjuntarArchivo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdjuntarArchivosActivity.class);
            intent.putParcelableArrayListExtra("archivosAdjuntos", (ArrayList<? extends Parcelable>) archivosAdjuntos);
            startActivityForResult(intent, 200);
        });

        cargarMantenedores();
        if (modoEditar) {
            cargarActividadExistente();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            archivosAdjuntos = data.getParcelableArrayListExtra("archivosAdjuntos");
            mostrarResumenArchivos();
        }
    }


    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etCupo = findViewById(R.id.etCupo);
        etDiasAviso = findViewById(R.id.etDiasAviso);
        etFechaPuntual = findViewById(R.id.etFechaPuntual);
        etHoraPuntual = findViewById(R.id.etHoraPuntual);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etHoraPeriodica = findViewById(R.id.etHoraPeriodica);
        etRepeticiones = findViewById(R.id.etRepeticiones);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerProyecto = findViewById(R.id.spinnerProyecto);
        spinnerOferente = findViewById(R.id.spinnerOferente);
        spinnerSocio = findViewById(R.id.spinnerSocio);
        spinnerLugar = findViewById(R.id.spinnerLugar);

        chipGroupDias = findViewById(R.id.chipGroupDias);
        btnGuardar = findViewById(R.id.btnGuardar);


        layoutFechaPuntual = findViewById(R.id.layoutFechaPuntual);
        layoutFechasPeriodicas = findViewById(R.id.layoutFechasPeriodicas);

        rgPeriodicidad = findViewById(R.id.rgPeriodicidad);
        rbPuntual = findViewById(R.id.rbPuntual);
        rbPeriodica = findViewById(R.id.rbPeriodica);
    }

    private void configurarEventos() {
        spinnerLugar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarCupoDesdeLugar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPuntual) {
                layoutFechaPuntual.setVisibility(View.VISIBLE);
                layoutFechasPeriodicas.setVisibility(View.GONE);
            } else {
                layoutFechaPuntual.setVisibility(View.GONE);
                layoutFechasPeriodicas.setVisibility(View.VISIBLE);
            }
        });


        etFechaPuntual.setOnClickListener(v -> mostrarDatePicker(1));
        etHoraPuntual.setOnClickListener(v -> mostrarTimePicker(1));
        etHoraPuntual.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarTimePicker(1);
        });
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(2));
        etHoraPeriodica.setOnClickListener(v -> mostrarTimePicker(2));
        etHoraPeriodica.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) mostrarTimePicker(2);
        });

        //

        btnGuardar.setOnClickListener(v -> {
            if (archivosAdjuntos != null && !archivosAdjuntos.isEmpty()) {
                for (ArchivoAdjunto archivo : archivosAdjuntos) {
                    archivoDAO.subirArchivo(archivo)
                            .addOnSuccessListener(taskSnapshot -> {
                                archivoDAO.guardarArchivo(archivo)
                                        .addOnSuccessListener(ref ->
                                                Toast.makeText(this, "Archivo subido: " + archivo.getNombre(), Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                                        );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al subir archivo: " + archivo.getNombre(), Toast.LENGTH_SHORT).show()
                            );
                }
            }

            guardarActividad();
            Toast.makeText(this, "Actividad guardada correctamente", Toast.LENGTH_SHORT).show();
            finish();
        });

    }
    private void mostrarResumenArchivos() {
        layoutArchivosAdjuntos.removeAllViews();
        if (archivosAdjuntos == null || archivosAdjuntos.isEmpty()) {
            layoutArchivosAdjuntos.setVisibility(View.GONE);
            return;
        }
        layoutArchivosAdjuntos.setVisibility(View.VISIBLE);
        for (ArchivoAdjunto archivo : archivosAdjuntos) {
            TextView tvArchivo = new TextView(this);
            tvArchivo.setText("📄 " + archivo.getNombre());
            layoutArchivosAdjuntos.addView(tvArchivo);
        }
    }
    private void mostrarDatePicker(int tipo) {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth);
                    String fechaFormateada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    if (tipo == 1) {
                        fechaPuntualSeleccionada = fechaSeleccionada;
                        etFechaPuntual.setText(fechaFormateada);
                    } else {
                        fechaInicioSeleccionada = fechaSeleccionada;
                        etFechaInicio.setText(fechaFormateada);
                    }
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void mostrarTimePicker(int tipo) {
        LocalTime horaInicial = LocalTime.of(9, 0);
        if (tipo == 1 && horaPuntualSeleccionada != null) {
            horaInicial = horaPuntualSeleccionada;
        } else if (tipo == 2 && horaPeriodicaSeleccionada != null) {
            horaInicial = horaPeriodicaSeleccionada;
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    LocalTime horaSeleccionada = LocalTime.of(hourOfDay, minute);
                    String horaFormateada = horaSeleccionada.format(formatoHora);
                    if (tipo == 1) {
                        horaPuntualSeleccionada = horaSeleccionada;
                        etHoraPuntual.setText(horaFormateada);
                    } else {
                        horaPeriodicaSeleccionada = horaSeleccionada;
                        etHoraPeriodica.setText(horaFormateada);
                    }
                },
                horaInicial.getHour(),
                horaInicial.getMinute(),
                true
        );
        timePickerDialog.show();
    }

    private void cargarMantenedores() {
        tipoActividadDAO.getAllTiposActividad(new TipoActividadDAO.OnTiposActividadLoadedListener() {
            @Override
            public void onTiposActividadLoaded(ArrayList<TipoActividad> tipos) {
                tiposActividad.clear();
                tiposActividad.addAll(tipos);
                poblarSpinner(spinnerTipo, extraerNombres(tiposActividad, TipoActividad::getNombre));
                tiposCargados = true;
                intentarConfigurarDatosEdicion();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CrearActividadActivity.this, "Error al cargar tipos de actividad", Toast.LENGTH_SHORT).show();
            }
        });

        proyectoDAO.getAllProyectos(new ProyectoDAO.OnProyectosLoadedListener() {
            @Override
            public void onProyectosLoaded(ArrayList<Proyecto> lista) {
                proyectos.clear();
                proyectos.addAll(lista);
                poblarSpinner(spinnerProyecto, extraerNombres(proyectos, Proyecto::getNombre));
                proyectosCargados = true;
                intentarConfigurarDatosEdicion();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CrearActividadActivity.this, "Error al cargar proyectos", Toast.LENGTH_SHORT).show();
            }
        });

        oferenteDAO.getAllOferentes(new OferenteActividadDAO.OnOferentesLoadedListener() {
            @Override
            public void onOferentesLoaded(ArrayList<OferenteActividad> lista) {
                oferentes.clear();
                oferentes.addAll(lista);
                poblarSpinner(spinnerOferente, extraerNombres(oferentes, OferenteActividad::getNombre));
                oferentesCargados = true;
                intentarConfigurarDatosEdicion();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CrearActividadActivity.this, "Error al cargar oferentes", Toast.LENGTH_SHORT).show();
            }
        });

        socioDAO.getAllSociosComunitarios(new SocioComunitarioDAO.OnSociosComunitariosLoadedListener() {
            @Override
            public void onSociosComunitariosLoaded(ArrayList<SocioComunitario> lista) {
                socios.clear();
                socios.addAll(lista);
                poblarSpinner(spinnerSocio, extraerNombres(socios, SocioComunitario::getNombre));
                sociosCargados = true;
                intentarConfigurarDatosEdicion();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CrearActividadActivity.this, "Error al cargar socios comunitarios", Toast.LENGTH_SHORT).show();
            }
        });

        lugarDAO.getAllLugares(new LugarDAO.OnLugaresLoadedListener() {
            @Override
            public void onLugaresLoaded(ArrayList<Lugar> lista) {
                lugares.clear();
                lugares.addAll(lista);
                poblarSpinner(spinnerLugar, extraerNombres(lugares, Lugar::getNombre));
                lugaresCargados = true;
                intentarConfigurarDatosEdicion();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CrearActividadActivity.this, "Error al cargar lugares", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void poblarSpinner(Spinner spinner, List<String> valores) {
        List<String> opciones = new ArrayList<>();
        opciones.add("Selecciona una opción");
        for (String valor : valores) {
            opciones.add(valor);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private <T> List<String> extraerNombres(List<T> lista, Function<T, String> extractor) {
        List<String> nombres = new ArrayList<>();
        for (T item : lista) {
            nombres.add(extractor.apply(item));
        }
        return nombres;
    }

    private void cargarActividadExistente() {
        actividadDAO.getActividadById(actividadId, new ActividadDAO.OnActividadLoadedListener() {
            @Override
            public void onActividadLoaded(Actividad actividad) {
                if (actividad == null) {
                    Toast.makeText(CrearActividadActivity.this, "No se pudo cargar la actividad", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                actividadEditar = actividad;
                actividadCargada = true;
                citaDAO.getCitasPorActividad(actividadEditar, new CitaDAO.OnCitasLoadedListener() {
                    @Override
                    public void onCitasLoaded(ArrayList<Cita> citas) {
                        citasCargadas = true;
                        intentarConfigurarDatosEdicion();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(CrearActividadActivity.this, "No fue posible cargar las citas", Toast.LENGTH_SHORT).show();
                        citasCargadas = true;
                        intentarConfigurarDatosEdicion();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CrearActividadActivity.this, "Error al cargar la actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void intentarConfigurarDatosEdicion() {
        if (!modoEditar) {
            return;
        }
        if (actividadCargada && citasCargadas && tiposCargados && proyectosCargados && oferentesCargados && sociosCargados && lugaresCargados) {
            cargarDatosActividad();
        }
    }

    private void cargarDatosActividad() {
        if (actividadEditar == null) {
            return;
        }
        etNombre.setText(actividadEditar.getNombre());
        if (actividadEditar.getCupo() != null) {
            etCupo.setText(String.valueOf(actividadEditar.getCupo()));
        }
        etDiasAviso.setText(String.valueOf(actividadEditar.getDiasAvisoPrevio()));
        if (actividadEditar.getProyecto() != null) {
            seleccionarItemEnSpinner(spinnerProyecto, proyectos, actividadEditar.getProyecto().getId(), Proyecto::getId);
        }
        if (!actividadEditar.getTiposActividad().isEmpty()) {
            TipoActividad tipo = actividadEditar.getTiposActividad().get(0);
            seleccionarItemEnSpinner(spinnerTipo, tiposActividad, tipo.getId(), TipoActividad::getId);
        }
        if (!actividadEditar.getOferentes().isEmpty()) {
            OferenteActividad oferente = actividadEditar.getOferentes().get(0);
            seleccionarItemEnSpinner(spinnerOferente, oferentes, oferente.getId(), OferenteActividad::getId);
        }
        if (actividadEditar.getSocioComunitario() != null) {
            seleccionarItemEnSpinner(spinnerSocio, socios, actividadEditar.getSocioComunitario().getId(), SocioComunitario::getId);
        }

        if (actividadEditar.getPeriodicidad().getTipo() == Periodicidad.Tipo.PUNTUAL) {
            rbPuntual.setChecked(true);
            actividadEditar.getPeriodicidad().getFechaInicio().ifPresent(fecha -> {
                fechaPuntualSeleccionada = fecha;
                etFechaPuntual.setText(fecha.format(formatoFecha));
            });
            if (!actividadEditar.getCitas().isEmpty()) {
                Cita cita = actividadEditar.getCitas().get(0);
                horaPuntualSeleccionada = cita.getHora();
                etHoraPuntual.setText(cita.getHora().format(formatoHora));
                seleccionarLugarDesdeCita(cita);
            }
        } else {
            rbPeriodica.setChecked(true);
            actividadEditar.getPeriodicidad().getFechaInicio().ifPresent(fecha -> {
                fechaInicioSeleccionada = fecha;
                etFechaInicio.setText(fecha.format(formatoFecha));
            });
            if (!actividadEditar.getCitas().isEmpty()) {
                Cita primera = actividadEditar.getCitas().get(0);
                horaPeriodicaSeleccionada = primera.getHora();
                etHoraPeriodica.setText(primera.getHora().format(formatoHora));
                seleccionarLugarDesdeCita(primera);

                Map<DayOfWeek, Integer> conteoDias = new EnumMap<>(DayOfWeek.class);
                for (Cita cita : actividadEditar.getCitas()) {
                    marcarChipParaDia(cita.getFecha().getDayOfWeek(), true);
                    conteoDias.put(cita.getFecha().getDayOfWeek(), conteoDias.getOrDefault(cita.getFecha().getDayOfWeek(), 0) + 1);
                }
                int maxRepeticiones = conteoDias.values().stream().max(Integer::compareTo).orElse(0);
                if (maxRepeticiones > 0) {
                    etRepeticiones.setText(String.valueOf(maxRepeticiones));
                }
            }
        }
    }

    private void seleccionarLugarDesdeCita(Cita cita) {
        if (cita.getLugar() == null) {
            return;
        }
        seleccionarItemEnSpinner(spinnerLugar, lugares, cita.getLugar().getId(), Lugar::getId);
    }

    private <T> void seleccionarItemEnSpinner(Spinner spinner, List<T> lista, String id, java.util.function.Function<T, String> extractor) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            String itemId = extractor.apply(lista.get(i));
            if (Objects.equals(id, itemId)) {
                spinner.setSelection(i + 1);
                break;
            }
        }
    }

    private void actualizarCupoDesdeLugar() {
        int indice = spinnerLugar.getSelectedItemPosition();
        if (indice <= 0) {
            etCupo.setEnabled(true);
            return;
        }
        Lugar lugarSeleccionado = lugares.get(indice - 1);
        Optional<Integer> cupoLugar = lugarSeleccionado.getCupo();
        if (cupoLugar.isPresent()) {
            etCupo.setText(String.valueOf(cupoLugar.get()));
            etCupo.setEnabled(false);
        } else {
            etCupo.setEnabled(true);
        }
    }

    private void guardarActividad() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre de la actividad", Toast.LENGTH_SHORT).show();
            return;
        }
        Lugar lugarSeleccionado = obtenerLugarSeleccionado();
        if (lugarSeleccionado == null) {
            Toast.makeText(this, "Seleccione un lugar", Toast.LENGTH_SHORT).show();
            return;
        }

        Periodicidad periodicidad = crearPeriodicidad();
        if (periodicidad == null) {
            return;
        }

        List<LocalDate> fechasCitas = generarFechasDeCitas(periodicidad.getTipo());
        if (fechasCitas == null) {
            return;
        }

        Actividad actividad = modoEditar ? actividadEditar : new Actividad(nombre, periodicidad);
        actividad.setNombre(nombre);
        actividad.setPeriodicidad(periodicidad);
        actividad.limpiarCitas();

        Integer cupo;
        try {
            cupo = obtenerCupoDesdeFormulario(lugarSeleccionado);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El cupo debe ser numérico", Toast.LENGTH_SHORT).show();
            return;
        }
        actividad.setCupo(cupo);

        int diasAviso = 0;
        String diasAvisoStr = etDiasAviso.getText() != null ? etDiasAviso.getText().toString().trim() : "";
        if (!diasAvisoStr.isEmpty()) {
            try {
                diasAviso = Integer.parseInt(diasAvisoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Los días de aviso deben ser numéricos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (diasAviso < 0) {
                Toast.makeText(this, "Los días de aviso no pueden ser negativos", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        actividad.setDiasAvisoPrevio(diasAviso);

        Proyecto proyecto = obtenerProyectoSeleccionado();
        if (proyecto == null) {
            Toast.makeText(this, "Seleccione un proyecto", Toast.LENGTH_SHORT).show();
            return;
        }
        actividad.setProyecto(proyecto);

        TipoActividad tipo = obtenerTipoSeleccionado();
        if (tipo == null) {
            Toast.makeText(this, "Seleccione un tipo de actividad", Toast.LENGTH_SHORT).show();
            return;
        }
        List<TipoActividad> tiposSeleccionados = new ArrayList<>();
        tiposSeleccionados.add(tipo);
        actividad.setTiposActividad(tiposSeleccionados);

        OferenteActividad oferente = obtenerOferenteSeleccionado();
        if (oferente == null) {
            Toast.makeText(this, "Seleccione un oferente", Toast.LENGTH_SHORT).show();
            return;
        }
        List<OferenteActividad> oferentesSeleccionados = new ArrayList<>();
        oferentesSeleccionados.add(oferente);
        actividad.setOferentes(oferentesSeleccionados);

        SocioComunitario socio = obtenerSocioSeleccionado();
        if (socio == null) {
            Toast.makeText(this, "Seleccione un socio comunitario", Toast.LENGTH_SHORT).show();
            return;
        }
        actividad.setSocioComunitario(socio);

        if (periodicidad.getTipo() == Periodicidad.Tipo.PUNTUAL) {
            actividad.crearCita(lugarSeleccionado, fechaPuntualSeleccionada, horaPuntualSeleccionada);
        } else {
            for (LocalDate fecha : fechasCitas) {
                actividad.crearCita(lugarSeleccionado, fecha, horaPeriodicaSeleccionada);
            }
        }
        btnGuardar.setEnabled(false);
        actividadDAO.saveActividad(actividad, new FirestoreOperationCallback() {
                    @Override
                    public void onSuccess() {
                        sincronizarCitas(actividad);
            }

                    @Override
                    public void onFailure(Exception exception) {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(CrearActividadActivity.this, "Error al guardar la actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sincronizarCitas(Actividad actividad) {
        citaDAO.deleteCitasPorActividad(actividad, new FirestoreOperationCallback() {
                    @Override
                    public void onSuccess() {
                        guardarCitas(actividad);
            }
                    @Override
                    public void onFailure(Exception exception) {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(CrearActividadActivity.this, "No fue posible actualizar las citas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void guardarCitas(Actividad actividad) {
        List<Cita> citas = actividad.getCitas();
        if (citas.isEmpty()) {
            finalizarEdicion();
            return;
        }
        AtomicInteger pendientes = new AtomicInteger(citas.size());
        AtomicBoolean errorReportado = new AtomicBoolean(false);
        for (Cita cita : citas) {
            citaDAO.saveCita(cita, new FirestoreOperationCallback() {
                @Override
                public void onSuccess() {
                    if (pendientes.decrementAndGet() == 0 && !errorReportado.get()) {
                        finalizarEdicion();
                    }
                }
                @Override
                public void onFailure(Exception exception) {
                    if (errorReportado.compareAndSet(false, true)) {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(CrearActividadActivity.this, "Error al guardar las citas", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void finalizarEdicion() {
        Toast.makeText(this, modoEditar ? "Actividad actualizada correctamente" : "Actividad creada correctamente", Toast.LENGTH_SHORT).show();
        btnGuardar.setEnabled(true);
        finish();
    }

    private Integer obtenerCupoDesdeFormulario(Lugar lugarSeleccionado) throws NumberFormatException {
        Optional<Integer> cupoLugar = lugarSeleccionado.getCupo();
        if (cupoLugar.isPresent()) {
            return cupoLugar.get();
        }
        String cupoStr = etCupo.getText() != null ? etCupo.getText().toString().trim() : "";
        if (cupoStr.isEmpty()) {
            return null;
        }
        return Integer.parseInt(cupoStr);
    }
    private Periodicidad crearPeriodicidad() {
        if (rbPuntual.isChecked()) {
            if (fechaPuntualSeleccionada == null) {
                Toast.makeText(this, "Seleccione la fecha de la actividad", Toast.LENGTH_SHORT).show();
                return null;
            }
            if (horaPuntualSeleccionada == null) {
                Toast.makeText(this, "Seleccione la hora de la actividad", Toast.LENGTH_SHORT).show();
                return null;
            }
            return Periodicidad.puntual("Única vez", fechaPuntualSeleccionada);
        }
        if (!rbPeriodica.isChecked()) {
            Toast.makeText(this, "Seleccione la periodicidad", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (fechaInicioSeleccionada == null) {
            Toast.makeText(this, "Seleccione la fecha de inicio", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (horaPeriodicaSeleccionada == null) {
            Toast.makeText(this, "Seleccione la hora de la actividad", Toast.LENGTH_SHORT).show();
            return null;
        }
        List<DayOfWeek> diasSeleccionados = obtenerDiasSeleccionados();
        if (diasSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un día", Toast.LENGTH_SHORT).show();
            return null;
        }
        int repeticiones = obtenerRepeticiones();
        if (repeticiones <= 0) {
            return null;
        }
        List<LocalDate> fechas = generarFechasPeriodicas(fechaInicioSeleccionada, diasSeleccionados, repeticiones);
        if (fechas.isEmpty()) {
            Toast.makeText(this, "No fue posible generar las citas", Toast.LENGTH_SHORT).show();
            return null;
        }
        LocalDate fechaInicio = fechas.stream().min(LocalDate::compareTo).orElse(fechaInicioSeleccionada);
        LocalDate fechaFin = fechas.stream().max(LocalDate::compareTo).orElse(fechaInicioSeleccionada);
        return Periodicidad.periodica("Periódica", fechaInicio, fechaFin);
    }
    private List<LocalDate> generarFechasDeCitas(Periodicidad.Tipo tipo) {
        if (tipo == Periodicidad.Tipo.PUNTUAL) {
            List<LocalDate> fechas = new ArrayList<>();
            fechas.add(fechaPuntualSeleccionada);
            return fechas;
        }
        List<DayOfWeek> diasSeleccionados = obtenerDiasSeleccionados();
        if (diasSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un día", Toast.LENGTH_SHORT).show();
            return null;
        }
        int repeticiones = obtenerRepeticiones();
        if (repeticiones <= 0) {
            return null;
        }
        return generarFechasPeriodicas(fechaInicioSeleccionada, diasSeleccionados, repeticiones);
    }
    private int obtenerRepeticiones() {
        String repeticionesStr = etRepeticiones.getText() != null ? etRepeticiones.getText().toString().trim() : "";
        if (repeticionesStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la cantidad de repeticiones", Toast.LENGTH_SHORT).show();
            return -1;
        }
        try {
            int repeticiones = Integer.parseInt(repeticionesStr);
            if (repeticiones <= 0) {
                Toast.makeText(this, "La cantidad de repeticiones debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return -1;
            }
            return repeticiones;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La cantidad de repeticiones debe ser numérica", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
    private Lugar obtenerLugarSeleccionado() {
        int indice = spinnerLugar.getSelectedItemPosition();
        if (indice <= 0) {
            return null;
        }
        return lugares.get(indice - 1);
    }
    private Proyecto obtenerProyectoSeleccionado() {
        int indice = spinnerProyecto.getSelectedItemPosition();
        if (indice <= 0) {
            return null;
        }
        return proyectos.get(indice - 1);
    }
    private TipoActividad obtenerTipoSeleccionado() {
        int indice = spinnerTipo.getSelectedItemPosition();
        if (indice <= 0) {
            return null;
        }
        return tiposActividad.get(indice - 1);
    }

    private OferenteActividad obtenerOferenteSeleccionado() {
        int indice = spinnerOferente.getSelectedItemPosition();
        if (indice <= 0) {
            return null;
        }

        return oferentes.get(indice - 1);
    }

    private SocioComunitario obtenerSocioSeleccionado() {
        int indice = spinnerSocio.getSelectedItemPosition();
        if (indice <= 0) {
            return null;
        }
        return socios.get(indice - 1);
    }

    private List<DayOfWeek> obtenerDiasSeleccionados() {
        List<DayOfWeek> dias = new ArrayList<>();
        for (int i = 0; i < chipGroupDias.getChildCount(); i++) {
            View chipView = chipGroupDias.getChildAt(i);
            if (chipView instanceof Chip) {
                Chip chip = (Chip) chipView;
                if (chip.isChecked()) {
                    DayOfWeek dia = obtenerDiaPorChipId(chip.getId());
                    if (dia != null) {
                        dias.add(dia);
                    }
                }
            }
        }
        return dias;
    }

    private DayOfWeek obtenerDiaPorChipId(int id) {
        if (id == R.id.chipLunes) return DayOfWeek.MONDAY;
        if (id == R.id.chipMartes) return DayOfWeek.TUESDAY;
        if (id == R.id.chipMiercoles) return DayOfWeek.WEDNESDAY;
        if (id == R.id.chipJueves) return DayOfWeek.THURSDAY;
        if (id == R.id.chipViernes) return DayOfWeek.FRIDAY;
        if (id == R.id.chipSabado) return DayOfWeek.SATURDAY;
        if (id == R.id.chipDomingo) return DayOfWeek.SUNDAY;
        return null;
    }

    private void marcarChipParaDia(DayOfWeek dia, boolean seleccionado) {
        int chipId;
        switch (dia) {
            case MONDAY:
                chipId = R.id.chipLunes;
                break;
            case TUESDAY:
                chipId = R.id.chipMartes;
                break;
            case WEDNESDAY:
                chipId = R.id.chipMiercoles;
                break;
            case THURSDAY:
                chipId = R.id.chipJueves;
                break;
            case FRIDAY:
                chipId = R.id.chipViernes;
                break;
            case SATURDAY:
                chipId = R.id.chipSabado;
                break;
            case SUNDAY:
                chipId = R.id.chipDomingo;
                break;
            default:
                return;
        }
        Chip chip = chipGroupDias.findViewById(chipId);
        if (chip != null) {
            chip.setChecked(seleccionado);
        }
    }

    private List<LocalDate> generarFechasPeriodicas(LocalDate fechaInicio, List<DayOfWeek> dias, int repeticiones) {
        List<LocalDate> fechas = new ArrayList<>();
        for (DayOfWeek dia : dias) {
            LocalDate primeraFecha = fechaInicio.with(TemporalAdjusters.nextOrSame(dia));
            for (int i = 0; i < repeticiones; i++) {
                fechas.add(primeraFecha.plusWeeks(i));
            }
        }
        return fechas;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}