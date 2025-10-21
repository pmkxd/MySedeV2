package com.test.mysede.actividades;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.test.mysede.R;
import com.test.mysede.model.Actividad;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.Periodicidad;
import com.test.mysede.model.Proyecto;
import com.test.mysede.model.SocioComunitario;
import com.test.mysede.model.TipoActividad;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CrearActividadActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etCupo, etDiasAviso;
    private TextInputEditText etFechaPuntual, etFechaInicio, etFechaFin;
    private Spinner spinnerTipo, spinnerProyecto, spinnerOferente, spinnerSocio;
    private RadioGroup rgPeriodicidad;
    private RadioButton rbPuntual, rbPeriodica;
    private View layoutFechaPuntual, layoutFechasPeriodicas;
    private Button btnGuardar;

    private boolean modoEditar = false;
    private int posicion = -1;
    private Actividad actividadEditar;

    private LocalDate fechaPuntualSeleccionada;
    private LocalDate fechaInicioSeleccionada;
    private LocalDate fechaFinSeleccionada;

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

        if (modoEditar && posicion != -1) {
            actividadEditar = ActividadHelper.obtenerActividadPorIndice(posicion);
            cargarDatosActividad();
        }
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etCupo = findViewById(R.id.etCupo);
        etDiasAviso = findViewById(R.id.etDiasAviso);
        etFechaPuntual = findViewById(R.id.etFechaPuntual);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerProyecto = findViewById(R.id.spinnerProyecto);
        spinnerOferente = findViewById(R.id.spinnerOferente);
        spinnerSocio = findViewById(R.id.spinnerSocio);

        rgPeriodicidad = findViewById(R.id.rgPeriodicidad);
        rbPuntual = findViewById(R.id.rbPuntual);
        rbPeriodica = findViewById(R.id.rbPeriodica);

        layoutFechaPuntual = findViewById(R.id.layoutFechaPuntual);
        layoutFechasPeriodicas = findViewById(R.id.layoutFechasPeriodicas);

        btnGuardar = findViewById(R.id.btnGuardar);
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
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(2));
        etFechaFin.setOnClickListener(v -> mostrarDatePicker(3));

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
                        case 3: // Fecha fin
                            fechaFinSeleccionada = fechaSeleccionada;
                            etFechaFin.setText(fechaFormateada);
                            break;
                    }
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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
                etFechaPuntual.setText(String.format("%02d/%02d/%d",
                        fechaPuntualSeleccionada.getDayOfMonth(),
                        fechaPuntualSeleccionada.getMonthValue(),
                        fechaPuntualSeleccionada.getYear()));
            }
        } else {
            rbPeriodica.setChecked(true);
            if (actividadEditar.getPeriodicidad().getFechaInicio().isPresent()) {
                fechaInicioSeleccionada = actividadEditar.getPeriodicidad().getFechaInicio().get();
                etFechaInicio.setText(String.format("%02d/%02d/%d",
                        fechaInicioSeleccionada.getDayOfMonth(),
                        fechaInicioSeleccionada.getMonthValue(),
                        fechaInicioSeleccionada.getYear()));
            }
            if (actividadEditar.getPeriodicidad().getFechaFin().isPresent()) {
                fechaFinSeleccionada = actividadEditar.getPeriodicidad().getFechaFin().get();
                etFechaFin.setText(String.format("%02d/%02d/%d",
                        fechaFinSeleccionada.getDayOfMonth(),
                        fechaFinSeleccionada.getMonthValue(),
                        fechaFinSeleccionada.getYear()));
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

        // Crear periodicidad
        Periodicidad periodicidad;
        if (rbPuntual.isChecked()) {
            if (fechaPuntualSeleccionada == null) {
                Toast.makeText(this, "Seleccione la fecha de la actividad", Toast.LENGTH_SHORT).show();
                return;
            }
            periodicidad = Periodicidad.puntual("Única vez", fechaPuntualSeleccionada);
        } else {
            if (fechaInicioSeleccionada == null || fechaFinSeleccionada == null) {
                Toast.makeText(this, "Seleccione las fechas de inicio y fin", Toast.LENGTH_SHORT).show();
                return;
            }
            periodicidad = Periodicidad.periodica("Periódica", fechaInicioSeleccionada, fechaFinSeleccionada);
        }

        // Crear o actualizar actividad
        Actividad actividad;
        if (modoEditar) {
            actividad = actividadEditar;
            actividad.setNombre(nombre);
            actividad.setPeriodicidad(periodicidad);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}