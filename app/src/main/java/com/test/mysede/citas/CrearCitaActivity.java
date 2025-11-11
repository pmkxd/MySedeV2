package com.test.mysede.citas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.textview.MaterialTextView;
import com.test.mysede.R;

// ============================================
// IMPORTS DEL SISTEMA DE PERMISOS
// ============================================
import com.test.mysede.auth.PermissionManager;
import com.test.mysede.auth.Permiso;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CrearCitaActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView actividadInput;
    private MaterialAutoCompleteTextView lugarInput;
    private TextInputEditText fechaInput;
    private TextInputEditText horaInput;
    private TextInputEditText cupoInput;
    private TextInputLayout actividadLayout;
    private TextInputLayout lugarLayout;
    private TextInputLayout fechaLayout;
    private TextInputLayout horaLayout;
    private TextInputLayout cupoLayout;
    private MaterialCardView resumenCard;
    private MaterialTextView resumenText;
    private MaterialButton confirmarButton;

    @Nullable
    private LocalDate fechaSeleccionada;
    @Nullable
    private LocalTime horaSeleccionada;

    private final DateTimeFormatter fechaFormatter =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter horaFormatter =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ============================================
        // VALIDAR PERMISO PARA CREAR CITAS
        // ============================================
        if (!PermissionManager.tienePermiso(Permiso.CREAR_CITA)) {
            Toast.makeText(this, "No tienes permiso para crear citas", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // FIN VALIDACIÓN DE PERMISOS

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_cita);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.crear_cita_root), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        actividadLayout = findViewById(R.id.crear_cita_layout_actividad);
        lugarLayout = findViewById(R.id.crear_cita_layout_lugar);
        fechaLayout = findViewById(R.id.crear_cita_layout_fecha);
        horaLayout = findViewById(R.id.crear_cita_layout_hora);
        cupoLayout = findViewById(R.id.crear_cita_layout_cupo);

        actividadInput = findViewById(R.id.crear_cita_input_actividad);
        lugarInput = findViewById(R.id.crear_cita_input_lugar);
        fechaInput = findViewById(R.id.crear_cita_input_fecha);
        horaInput = findViewById(R.id.crear_cita_input_hora);
        cupoInput = findViewById(R.id.crear_cita_input_cupo);
        resumenCard = findViewById(R.id.crear_cita_resumen_card);
        resumenText = findViewById(R.id.crear_cita_resumen_text);
        confirmarButton = findViewById(R.id.crear_cita_btn_confirmar);

        configurarListasDesplegables();
        configurarFecha();
        configurarHora();
        configurarConfirmacion();
    }

    private void configurarListasDesplegables() {
        ArrayAdapter<String> actividadesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.actividades_demo));
        actividadInput.setAdapter(actividadesAdapter);

        ArrayAdapter<String> lugaresAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.lugares_demo));
        lugarInput.setAdapter(lugaresAdapter);
    }

    private void configurarFecha() {
        View.OnClickListener listener = v -> mostrarSelectorFecha();
        fechaInput.setOnClickListener(listener);
        fechaInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarSelectorFecha();
            }
        });
    }

    private void configurarHora() {
        View.OnClickListener listener = v -> mostrarSelectorHora();
        horaInput.setOnClickListener(listener);
        horaInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarSelectorHora();
            }
        });
    }

    private void configurarConfirmacion() {
        confirmarButton.setOnClickListener(v -> {
            // ============================================
            // VALIDACIÓN ADICIONAL ANTES DE CONFIRMAR
            // ============================================
            if (!PermissionManager.tienePermiso(Permiso.CREAR_CITA)) {
                Toast.makeText(this, "No tienes permiso para crear citas", Toast.LENGTH_SHORT).show();
                return;
            }
            // FIN VALIDACIÓN ADICIONAL

            limpiarErrores();
            if (!validarCampos()) {
                Snackbar.make(v, R.string.crear_cita_campos_obligatorios, Snackbar.LENGTH_LONG).show();
                return;
            }

            String actividad = obtenerTexto(actividadInput);
            String lugar = obtenerTexto(lugarInput);
            String cupo = obtenerTexto(cupoInput);
            String fecha = fechaSeleccionada != null ? fechaSeleccionada.format(fechaFormatter) : "";
            String hora = horaSeleccionada != null ? horaSeleccionada.format(horaFormatter) : "";

            resumenText.setText(getString(R.string.crear_cita_resumen_text, actividad, lugar, fecha, hora, cupo));
            resumenCard.setVisibility(View.VISIBLE);
            Snackbar.make(v, R.string.crear_cita_snackbar_confirmada, Snackbar.LENGTH_LONG).show();
        });
    }

    private void mostrarSelectorFecha() {
        long seleccionInicial = MaterialDatePicker.todayInUtcMilliseconds();
        if (fechaSeleccionada != null) {
            seleccionInicial = fechaSeleccionada.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        // Configurar restricciones para bloquear fechas pasadas
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.crear_cita_hint_fecha)
                .setSelection(seleccionInicial)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) {
                return;
            }
            fechaSeleccionada = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            fechaInput.setText(fechaSeleccionada.format(fechaFormatter));
        });
        datePicker.show(getSupportFragmentManager(), "crear_cita_fecha");
    }

    private void mostrarSelectorHora() {
        int horaInicial = horaSeleccionada != null ? horaSeleccionada.getHour() : 9;
        int minutosIniciales = horaSeleccionada != null ? horaSeleccionada.getMinute() : 0;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(horaInicial)
                .setMinute(minutosIniciales)
                .setTitleText(R.string.crear_cita_hint_hora)
                .build();
        picker.addOnPositiveButtonClickListener(dialog -> {
            horaSeleccionada = LocalTime.of(picker.getHour(), picker.getMinute());
            horaInput.setText(horaSeleccionada.format(horaFormatter));
        });
        picker.show(getSupportFragmentManager(), "crear_cita_hora");
    }

    private boolean validarCampos() {
        boolean valido = true;
        if (TextUtils.isEmpty(obtenerTexto(actividadInput))) {
            actividadLayout.setError(getString(R.string.crear_cita_campos_obligatorios));
            valido = false;
        }
        if (TextUtils.isEmpty(obtenerTexto(lugarInput))) {
            lugarLayout.setError(getString(R.string.crear_cita_campos_obligatorios));
            valido = false;
        }
        if (fechaSeleccionada == null) {
            fechaLayout.setError(getString(R.string.crear_cita_campos_obligatorios));
            valido = false;
        }
        if (horaSeleccionada == null) {
            horaLayout.setError(getString(R.string.crear_cita_campos_obligatorios));
            valido = false;
        }
        // Validar que la combinación fecha + hora no esté en el pasado
        if (fechaSeleccionada != null && horaSeleccionada != null) {
            if (fechaSeleccionada.equals(LocalDate.now()) &&
                horaSeleccionada.isBefore(LocalTime.now())) {
                horaLayout.setError("La hora no puede ser anterior a la actual para hoy");
                valido = false;
            }
        }
        if (TextUtils.isEmpty(obtenerTexto(cupoInput))) {
            cupoLayout.setError(getString(R.string.crear_cita_campos_obligatorios));
            valido = false;
        }
        return valido;
    }

    private void limpiarErrores() {
        actividadLayout.setError(null);
        lugarLayout.setError(null);
        fechaLayout.setError(null);
        horaLayout.setError(null);
        cupoLayout.setError(null);
    }

    private String obtenerTexto(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private String obtenerTexto(MaterialAutoCompleteTextView autoCompleteTextView) {
        CharSequence value = autoCompleteTextView.getText();
        return value != null ? value.toString().trim() : "";
    }
}