package com.test.mysede.citas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.test.mysede.R;
import com.test.mysede.model.Cita;
import com.test.mysede.model.sample.CitaSamples;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReagendarCitaActivity extends AppCompatActivity {

    private Cita citaProgramada;
    private MaterialTextView resumenDetalle;
    private MaterialTextView resumenAviso;
    private MaterialAutoCompleteTextView nuevoLugarInput;
    private TextInputEditText nuevaFechaInput;
    private TextInputEditText nuevaHoraInput;
    private TextInputLayout nuevoLugarLayout;
    private TextInputLayout nuevaFechaLayout;
    private TextInputLayout nuevaHoraLayout;
    private MaterialSwitch notificarSwitch;
    private MaterialCardView resumenNuevoCard;
    private MaterialTextView resumenNuevoText;

    @Nullable
    private LocalDate nuevaFecha;
    @Nullable
    private LocalTime nuevaHora;

    private final DateTimeFormatter fechaFormatter =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter horaFormatter =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reagendar_cita);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reagendar_cita_root), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        citaProgramada = CitaSamples.proximaCitaDemostracion();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        resumenDetalle = findViewById(R.id.reagendar_cita_resumen_detalle);
        resumenAviso = findViewById(R.id.reagendar_cita_resumen_aviso);
        nuevoLugarLayout = findViewById(R.id.reagendar_cita_layout_lugar);
        nuevaFechaLayout = findViewById(R.id.reagendar_cita_layout_fecha);
        nuevaHoraLayout = findViewById(R.id.reagendar_cita_layout_hora);
        nuevoLugarInput = findViewById(R.id.reagendar_cita_input_lugar);
        nuevaFechaInput = findViewById(R.id.reagendar_cita_input_fecha);
        nuevaHoraInput = findViewById(R.id.reagendar_cita_input_hora);
        notificarSwitch = findViewById(R.id.reagendar_cita_switch_notificar);
        resumenNuevoCard = findViewById(R.id.reagendar_cita_resumen_nuevo);
        resumenNuevoText = findViewById(R.id.reagendar_cita_resumen_nuevo_text);
        MaterialButton confirmarButton = findViewById(R.id.reagendar_cita_btn_confirmar);

        configurarResumenActual();
        configurarListas();
        configurarFecha();
        configurarHora();

        confirmarButton.setOnClickListener(v -> {
            limpiarErrores();
            if (!validarCampos()) {
                Snackbar.make(v, R.string.reagendar_cita_error_campos, Snackbar.LENGTH_LONG).show();
                return;
            }

            String lugar = obtenerTexto(nuevoLugarInput);
            String fecha = nuevaFecha != null ? nuevaFecha.format(fechaFormatter) : "";
            String hora = nuevaHora != null ? nuevaHora.format(horaFormatter) : "";
            String notificacion = notificarSwitch.isChecked()
                    ? getString(R.string.accion_notificar_si)
                    : getString(R.string.accion_notificar_no);

            resumenNuevoText.setText(getString(R.string.reagendar_cita_resumen_nuevo_text, lugar, fecha, hora, notificacion));
            resumenNuevoCard.setVisibility(View.VISIBLE);
            Snackbar.make(v, R.string.reagendar_cita_snackbar_guardada, Snackbar.LENGTH_LONG).show();
        });
    }

    private void configurarResumenActual() {
        String detalle = getString(
                R.string.reagendar_cita_resumen_detalle_formato,
                citaProgramada.getActividad().getNombre(),
                citaProgramada.getLugar().getNombre(),
                citaProgramada.getFecha().format(fechaFormatter),
                citaProgramada.getHora().format(horaFormatter)
        );
        resumenDetalle.setText(detalle);

        LocalDateTime momentoAviso = citaProgramada.getActividad().calcularMomentoAviso(citaProgramada);
        resumenAviso.setText(getString(
                R.string.reagendar_cita_aviso_programado,
                momentoAviso.toLocalDate().format(fechaFormatter),
                momentoAviso.toLocalTime().format(horaFormatter)
        ));
    }

    private void configurarListas() {
        ArrayAdapter<String> lugaresAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.lugares_demo));
        nuevoLugarInput.setAdapter(lugaresAdapter);
    }

    private void configurarFecha() {
        View.OnClickListener listener = v -> mostrarSelectorFecha();
        nuevaFechaInput.setOnClickListener(listener);
        nuevaFechaInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarSelectorFecha();
            }
        });
    }

    private void configurarHora() {
        View.OnClickListener listener = v -> mostrarSelectorHora();
        nuevaHoraInput.setOnClickListener(listener);
        nuevaHoraInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarSelectorHora();
            }
        });
    }

    private void mostrarSelectorFecha() {
        long seleccionInicial = MaterialDatePicker.todayInUtcMilliseconds();
        if (nuevaFecha != null) {
            seleccionInicial = nuevaFecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.reagendar_cita_hint_fecha)
                .setSelection(seleccionInicial)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) {
                return;
            }
            nuevaFecha = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            nuevaFechaInput.setText(nuevaFecha.format(fechaFormatter));
        });
        picker.show(getSupportFragmentManager(), "reagendar_cita_fecha");
    }

    private void mostrarSelectorHora() {
        int horaInicial = nuevaHora != null ? nuevaHora.getHour() : citaProgramada.getHora().getHour();
        int minutosIniciales = nuevaHora != null ? nuevaHora.getMinute() : citaProgramada.getHora().getMinute();

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(horaInicial)
                .setMinute(minutosIniciales)
                .setTitleText(R.string.reagendar_cita_hint_hora)
                .build();
        picker.addOnPositiveButtonClickListener(dialog -> {
            nuevaHora = LocalTime.of(picker.getHour(), picker.getMinute());
            nuevaHoraInput.setText(nuevaHora.format(horaFormatter));
        });
        picker.show(getSupportFragmentManager(), "reagendar_cita_hora");
    }

    private boolean validarCampos() {
        boolean valido = true;
        if (TextUtils.isEmpty(obtenerTexto(nuevoLugarInput))) {
            nuevoLugarLayout.setError(getString(R.string.reagendar_cita_error_campos));
            valido = false;
        }
        if (nuevaFecha == null) {
            nuevaFechaLayout.setError(getString(R.string.reagendar_cita_error_campos));
            valido = false;
        }
        if (nuevaHora == null) {
            nuevaHoraLayout.setError(getString(R.string.reagendar_cita_error_campos));
            valido = false;
        }
        return valido;
    }

    private void limpiarErrores() {
        nuevoLugarLayout.setError(null);
        nuevaFechaLayout.setError(null);
        nuevaHoraLayout.setError(null);
    }

    private String obtenerTexto(MaterialAutoCompleteTextView autoCompleteTextView) {
        CharSequence value = autoCompleteTextView.getText();
        return value != null ? value.toString().trim() : "";
    }
}