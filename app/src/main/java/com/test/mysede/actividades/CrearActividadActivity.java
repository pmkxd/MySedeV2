package com.test.mysede.actividades;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.test.mysede.R;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.Periodicidad;
import com.test.mysede.model.Proyecto;
import com.test.mysede.model.SocioComunitario;
import com.test.mysede.model.TipoActividad;
import com.test.mysede.model.Lugar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CrearActividadActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etCupo, etDiasAviso;
    private TextInputEditText etFechaPuntual, etHoraPuntual, etFechaInicio, etHoraPeriodica, etRepeticiones;
    private Spinner spinnerTipo, spinnerProyecto, spinnerOferente, spinnerSocio, spinnerLugar;
    private RadioGroup rgPeriodicidad;
    private RadioButton rbPuntual, rbPeriodica;
    private View layoutFechaPuntual, layoutFechasPeriodicas;
    private Button btnGuardar;
    private ChipGroup chipGroupDias;

    private boolean modoEditar = false;
    private int posicion = -1;
    private Actividad actividadEditar;

    private LocalDate fechaPuntualSeleccionada;
    private LocalDate fechaInicioSeleccionada;
    private LocalTime horaPuntualSeleccionada;
    private LocalTime horaPeriodicaSeleccionada;

    private static final int PICK_FILE_REQUEST = 123;
    private Button btnAdjuntarArchivo;
    private LinearLayout layoutArchivosAdjuntos;
    private ArrayList<Uri> urisDeArchivos = new ArrayList<>();

    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "CL"));
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        // Verificar si es modo editar
        modoEditar = "editar".equals(getIntent().getStringExtra("modo"));
        posicion = getIntent().getIntExtra("posicion", -1);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(modoEditar ? "Editar Actividad" : "Crear Actividad");
        }

        inicializarVistas();
        configurarSpinners();
        configurarEventos();
        btnAdjuntarArchivo = findViewById(R.id.btnAdjuntarArchivo);
        layoutArchivosAdjuntos = findViewById(R.id.layoutArchivosAdjuntos);

        btnAdjuntarArchivo.setOnClickListener(v -> {
            abrirSelectorDeArchivos();
        });
        if (modoEditar && posicion != -1) {
            actividadEditar = ActividadHelper.obtenerActividadPorIndice(posicion);
            cargarDatosActividad();
        }
    }
    private void abrirSelectorDeArchivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Permite seleccionar cualquier tipo de archivo
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permite selección múltiple
        startActivityForResult(Intent.createChooser(intent, "Selecciona los archivos"), PICK_FILE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                urisDeArchivos.clear(); // Limpiar la lista anterior por si se vuelve a seleccionar
                if (data.getClipData() != null) {
                    // El usuario seleccionó múltiples archivos
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        urisDeArchivos.add(uri);
                    }
                } else if (data.getData() != null) {
                    // El usuario seleccionó un solo archivo
                    Uri uri = data.getData();
                    urisDeArchivos.add(uri);
                }
                actualizarVistaDeArchivosAdjuntos();
            }
        }
    }

    private void actualizarVistaDeArchivosAdjuntos() {
        layoutArchivosAdjuntos.removeAllViews(); // Limpiar vistas anteriores

        if (urisDeArchivos.isEmpty()) {
            layoutArchivosAdjuntos.setVisibility(View.GONE);
            return;
        }

        layoutArchivosAdjuntos.setVisibility(View.VISIBLE);

        for (Uri uri : urisDeArchivos) {
            // Crea un TextView para mostrar el nombre del archivo
            TextView tvNombreArchivo = new TextView(this);
            // Aquí podrías implementar una función para obtener el nombre del archivo a partir de la Uri
            tvNombreArchivo.setText("Archivo: " + getFileName(uri));
            tvNombreArchivo.setPadding(8, 8, 8, 8);
            layoutArchivosAdjuntos.addView(tvNombreArchivo);
        }
    }

    // Método auxiliar para obtener el nombre del archivo desde una Uri
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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

        rgPeriodicidad = findViewById(R.id.rgPeriodicidad);
        rbPuntual = findViewById(R.id.rbPuntual);
        rbPeriodica = findViewById(R.id.rbPeriodica);

        layoutFechaPuntual = findViewById(R.id.layoutFechaPuntual);
        layoutFechasPeriodicas = findViewById(R.id.layoutFechasPeriodicas);

        btnGuardar = findViewById(R.id.btnGuardar);
        chipGroupDias = findViewById(R.id.chipGroupDias);
    }

    private void configurarSpinners() {
        // Spinner Tipo de Actividad
        String[] tipos = {"Taller", "Capacitación", "Charla", "Operativo", "Diagnóstico"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        // Spinner Proyecto
        String[] proyectos = {"Salud Comunitaria", "Desarrollo Económico Local",
                "Fortalecimiento Comunitario", "Salud en Territorio", "Planificación Territorial"};
        ArrayAdapter<String> adapterProyecto = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, proyectos);
        adapterProyecto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyecto.setAdapter(adapterProyecto);

        // Spinner Oferente
        String[] oferentes = {"Cruz Roja", "INACAP", "Universidad de Los Lagos",
                "Universidad Austral", "Centro de Salud Alerce"};
        ArrayAdapter<String> adapterOferente = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, oferentes);
        adapterOferente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOferente.setAdapter(adapterOferente);

        // Spinner Socio Comunitario
        String[] socios = {"Junta de Vecinos N°5", "Asociación de Microempresarios",
                "Centro de Adultos Mayores", "Comité de Salud Rural", "Mesa Territorial Alerce"};
        ArrayAdapter<String> adapterSocio = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, socios);
        adapterSocio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocio.setAdapter(adapterSocio);

        // Spinner Lugar
        String[] lugares = getResources().getStringArray(R.array.lugares_demo);
        ArrayAdapter<String> adapterLugar = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, lugares);
        adapterLugar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLugar.setAdapter(adapterLugar);
    }

    private void configurarEventos() {
        // RadioGroup periodicidad
        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPuntual) {
                layoutFechaPuntual.setVisibility(View.VISIBLE);
                layoutFechasPeriodicas.setVisibility(View.GONE);
            } else {
                layoutFechaPuntual.setVisibility(View.GONE);
                layoutFechasPeriodicas.setVisibility(View.VISIBLE);
            }
        });

        // DatePickers
        etFechaPuntual.setOnClickListener(v -> mostrarDatePicker(1));
        etHoraPuntual.setOnClickListener(v -> mostrarTimePicker(1));
        etHoraPuntual.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) mostrarTimePicker(1); });
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(2));
        etHoraPeriodica.setOnClickListener(v -> mostrarTimePicker(2));
        etHoraPeriodica.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) mostrarTimePicker(2); });

        // Botón guardar
        btnGuardar.setOnClickListener(v -> guardarActividad());
    }

    private void mostrarDatePicker(int tipo) {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth);
                    String fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

                    switch (tipo) {
                        case 1: // Fecha puntual
                            fechaPuntualSeleccionada = fechaSeleccionada;
                            etFechaPuntual.setText(fechaFormateada);
                            break;
                        case 2: // Fecha inicio
                            fechaInicioSeleccionada = fechaSeleccionada;
                            etFechaInicio.setText(fechaFormateada);
                            break;
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

    private void cargarDatosActividad() {
        if (actividadEditar == null) return;

        etNombre.setText(actividadEditar.getNombre());

        if (actividadEditar.getCupo() != null) {
            etCupo.setText(String.valueOf(actividadEditar.getCupo()));
        }

        etDiasAviso.setText(String.valueOf(actividadEditar.getDiasAvisoPrevio()));

        // Periodicidad
        if (actividadEditar.getPeriodicidad().getTipo() == Periodicidad.Tipo.PUNTUAL) {
            rbPuntual.setChecked(true);
            if (actividadEditar.getPeriodicidad().getFechaInicio().isPresent()) {
                fechaPuntualSeleccionada = actividadEditar.getPeriodicidad().getFechaInicio().get();
                etFechaPuntual.setText(fechaPuntualSeleccionada.format(formatoFecha));
            }
            List<com.test.mysede.model.Cita> citas = actividadEditar.getCitas();
            if (!citas.isEmpty()) {
                horaPuntualSeleccionada = citas.get(0).getHora();
                etHoraPuntual.setText(horaPuntualSeleccionada.format(formatoHora));
            }
        } else {
            rbPeriodica.setChecked(true);
                actividadEditar.getPeriodicidad().getFechaInicio().ifPresent(fechaInicio -> {
                    fechaInicioSeleccionada = fechaInicio;
                    etFechaInicio.setText(fechaInicioSeleccionada.format(formatoFecha));
                });

                List<com.test.mysede.model.Cita> citas = actividadEditar.getCitas();
                if (!citas.isEmpty()) {
                    horaPeriodicaSeleccionada = citas.get(0).getHora();
                    etHoraPeriodica.setText(horaPeriodicaSeleccionada.format(formatoHora));

                    // Mapear días seleccionados y contar repeticiones
                    java.util.Map<DayOfWeek, Integer> conteoDias = new java.util.EnumMap<>(DayOfWeek.class);
                    int maxRepeticiones = 0;
                    for (com.test.mysede.model.Cita cita : citas) {
                        DayOfWeek dia = cita.getFecha().getDayOfWeek();
                        int conteo = conteoDias.containsKey(dia) ? conteoDias.get(dia) + 1 : 1;
                        conteoDias.put(dia, conteo);
                        if (conteo > maxRepeticiones) {
                            maxRepeticiones = conteo;
                        }
                    }

                    for (DayOfWeek dia : conteoDias.keySet()) {
                        marcarChipParaDia(dia, true);
                    }

                    if (maxRepeticiones > 0) {
                        etRepeticiones.setText(String.valueOf(maxRepeticiones));
                    }
                }
            }
        }

        private void guardarActividad() {
            // Validar campos
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingrese el nombre de la actividad", Toast.LENGTH_SHORT).show();
                return;
            }

            String lugarNombre = spinnerLugar.getSelectedItem().toString();
            Lugar.Tipo tipoLugar = obtenerTipoLugarPorNombre(lugarNombre);

            // Crear periodicidad
            Periodicidad periodicidad;
            List<LocalDate> fechasCitas = new ArrayList<>();
            if (rbPuntual.isChecked()) {
                if (fechaPuntualSeleccionada == null) {
                    Toast.makeText(this, "Seleccione la fecha de la actividad", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (horaPuntualSeleccionada == null) {
                    Toast.makeText(this, "Seleccione la hora de la actividad", Toast.LENGTH_SHORT).show();
                    return;
                }
                periodicidad = Periodicidad.puntual("Única vez", fechaPuntualSeleccionada);
                fechasCitas.add(fechaPuntualSeleccionada);
            } else {
                    if (fechaInicioSeleccionada == null) {
                        Toast.makeText(this, "Seleccione la fecha de inicio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (horaPeriodicaSeleccionada == null) {
                        Toast.makeText(this, "Seleccione la hora de la actividad", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DayOfWeek> diasSeleccionados = obtenerDiasSeleccionados();
                    if (diasSeleccionados.isEmpty()) {
                        Toast.makeText(this, "Seleccione al menos un día", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String repeticionesStr = etRepeticiones.getText().toString().trim();
                    if (repeticionesStr.isEmpty()) {
                        Toast.makeText(this, "Ingrese la cantidad de repeticiones", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int repeticiones = Integer.parseInt(repeticionesStr);
                    if (repeticiones <= 0) {
                        Toast.makeText(this, "La cantidad de repeticiones debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    fechasCitas = generarFechasPeriodicas(fechaInicioSeleccionada, diasSeleccionados, repeticiones);
                    if (fechasCitas.isEmpty()) {
                        Toast.makeText(this, "No fue posible generar las citas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LocalDate fechaInicio = fechasCitas.stream().min(LocalDate::compareTo).orElse(fechaInicioSeleccionada);
                    LocalDate fechaFin = fechasCitas.stream().max(LocalDate::compareTo).orElse(fechaInicioSeleccionada);
                    periodicidad = Periodicidad.periodica("Periódica", fechaInicio, fechaFin);
                }

                // Crear o actualizar actividad
                Actividad actividad;
                if (modoEditar) {
                    actividad = actividadEditar;
                    actividad.setNombre(nombre);
                    actividad.setPeriodicidad(periodicidad);
                    actividad.limpiarCitas();
                } else {
                    actividad = new Actividad(nombre, periodicidad);
                }

                // Configurar cupo
                String cupoStr = etCupo.getText().toString().trim();
                if (!cupoStr.isEmpty()) {
                    actividad.setCupo(Integer.parseInt(cupoStr));
                }

                // Configurar días de aviso
                String diasAvisoStr = etDiasAviso.getText().toString().trim();
                if (!diasAvisoStr.isEmpty()) {
                    actividad.setDiasAvisoPrevio(Integer.parseInt(diasAvisoStr));
                }

                // Configurar proyecto
                String proyectoNombre = spinnerProyecto.getSelectedItem().toString();
                actividad.setProyecto(new Proyecto(proyectoNombre));

                // Configurar tipo de actividad
                String tipoNombre = spinnerTipo.getSelectedItem().toString();
                TipoActividad.Categoria categoria = obtenerCategoriaPorNombre(tipoNombre);
                List<TipoActividad> tipos = new ArrayList<>();
                tipos.add(new TipoActividad(tipoNombre, "Descripción de " + tipoNombre, categoria));
                actividad.setTiposActividad(tipos);

                // Configurar oferente
                String oferenteNombre = spinnerOferente.getSelectedItem().toString();
                OferenteActividad.Institucion institucion = obtenerInstitucionPorNombre(oferenteNombre);
                List<OferenteActividad> oferentes = new ArrayList<>();
                oferentes.add(new OferenteActividad(oferenteNombre, "Docente Responsable", institucion));
                actividad.setOferentes(oferentes);

                // Configurar socio comunitario
                String socioNombre = spinnerSocio.getSelectedItem().toString();
                actividad.setSocioComunitario(new SocioComunitario(socioNombre));

                // Crear citas asociadas
                Lugar lugar = new Lugar(lugarNombre, tipoLugar, actividad.getCupo());
                if (rbPuntual.isChecked()) {
                    actividad.crearCita(lugar, fechaPuntualSeleccionada, horaPuntualSeleccionada);
                } else {
                    for (LocalDate fechaCita : fechasCitas) {
                        actividad.crearCita(lugar, fechaCita, horaPeriodicaSeleccionada);
                    }
                }

                // Guardar
                if (modoEditar) {
                    ActividadHelper.actualizarActividad(posicion, actividad);
                    Toast.makeText(this, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    ActividadHelper.agregarActividad(actividad);
                    Toast.makeText(this, "Actividad creada correctamente", Toast.LENGTH_SHORT).show();
                }

                finish();
            }

            private TipoActividad.Categoria obtenerCategoriaPorNombre(String nombre) {
                switch (nombre) {
                    case "Taller":
                        return TipoActividad.Categoria.TALLER;
                    case "Capacitación":
                        return TipoActividad.Categoria.CAPACITACION;
                    case "Charla":
                        return TipoActividad.Categoria.CHARLA;
                    case "Operativo":
                        return TipoActividad.Categoria.OPERATIVO;
                    case "Diagnóstico":
                        return TipoActividad.Categoria.DIAGNOSTICO;
                    default:
                        return TipoActividad.Categoria.TALLER;
                }
            }

            private OferenteActividad.Institucion obtenerInstitucionPorNombre(String nombre) {
                if (nombre.contains("INACAP") || nombre.contains("Centro de Salud")) {
                    return OferenteActividad.Institucion.CFT;
                } else if (nombre.contains("Universidad")) {
                    return OferenteActividad.Institucion.UNIVERSIDAD;
                } else {
                    return OferenteActividad.Institucion.IP;
                }
            }

            private Lugar.Tipo obtenerTipoLugarPorNombre(String nombre) {
                if (nombre.toLowerCase(Locale.getDefault()).contains("sede")) {
                    return Lugar.Tipo.OFICINA_DEL_CENTRO;
                }
                return Lugar.Tipo.LUGAR_DEL_TERRITORIO;
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