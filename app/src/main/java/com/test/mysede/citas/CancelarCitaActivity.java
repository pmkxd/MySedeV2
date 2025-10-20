package com.test.mysede.citas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.test.mysede.R;
import com.test.mysede.model.Cita;
import com.test.mysede.model.sample.CitaSamples;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CancelarCitaActivity extends AppCompatActivity {

    private Cita citaProgramada;
    private MaterialTextView detalleText;
    private MaterialTextView avisoText;
    private TextInputLayout motivoLayout;
    private TextInputEditText motivoInput;
    private MaterialSwitch notificarSwitch;
    private MaterialCheckBox confirmacionCheck;
    private MaterialCardView resumenCard;
    private MaterialTextView resumenText;

    private final DateTimeFormatter fechaFormatter =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter horaFormatter =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cancelar_cita);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelar_cita_root), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        citaProgramada = CitaSamples.proximaCitaDemostracion();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        detalleText = findViewById(R.id.cancelar_cita_detalle_text);
        avisoText = findViewById(R.id.cancelar_cita_detalle_aviso);
        motivoLayout = findViewById(R.id.cancelar_cita_layout_motivo);
        motivoInput = findViewById(R.id.cancelar_cita_input_motivo);
        notificarSwitch = findViewById(R.id.cancelar_cita_switch_notificar);
        confirmacionCheck = findViewById(R.id.cancelar_cita_check_confirmacion);
        resumenCard = findViewById(R.id.cancelar_cita_resumen_card);
        resumenText = findViewById(R.id.cancelar_cita_resumen_text);
        MaterialButton confirmarButton = findViewById(R.id.cancelar_cita_btn_confirmar);
        MaterialButton mantenerButton = findViewById(R.id.cancelar_cita_btn_rechazar);

        configurarDetalle();
        confirmacionCheck.setOnCheckedChangeListener((buttonView, isChecked) -> confirmacionCheck.setError(null));

        confirmarButton.setOnClickListener(v -> {
            motivoLayout.setError(null);
            if (!validarFormulario()) {
                Snackbar.make(v, R.string.cancelar_cita_error_datos, Snackbar.LENGTH_LONG).show();
                return;
            }

            String motivo = motivoInput.getText() != null ? motivoInput.getText().toString().trim() : "";
            String notificacion = notificarSwitch.isChecked()
                    ? getString(R.string.accion_notificar_si)
                    : getString(R.string.accion_notificar_no);
            resumenText.setText(getString(R.string.cancelar_cita_resumen_text, notificacion, motivo));
            resumenCard.setVisibility(View.VISIBLE);
            Snackbar.make(v, R.string.cancelar_cita_snackbar_exitosa, Snackbar.LENGTH_LONG).show();
        });

        mantenerButton.setOnClickListener(v ->
                Snackbar.make(v, R.string.cancelar_cita_mensaje_mantener, Snackbar.LENGTH_LONG).show()
        );
    }

    private void configurarDetalle() {
        String detalle = getString(
                R.string.cancelar_cita_detalle_formato,
                citaProgramada.getActividad().getNombre(),
                citaProgramada.getLugar().getNombre(),
                citaProgramada.getFecha().format(fechaFormatter),
                citaProgramada.getHora().format(horaFormatter)
        );
        detalleText.setText(detalle);

        LocalDateTime momentoAviso = citaProgramada.getActividad().calcularMomentoAviso(citaProgramada);
        avisoText.setText(getString(
                R.string.cancelar_cita_aviso_programado,
                momentoAviso.toLocalDate().format(fechaFormatter),
                momentoAviso.toLocalTime().format(horaFormatter)
        ));
    }

    private boolean validarFormulario() {
        String motivo = motivoInput.getText() != null ? motivoInput.getText().toString().trim() : "";
        boolean valido = true;
        if (TextUtils.isEmpty(motivo)) {
            motivoLayout.setError(getString(R.string.cancelar_cita_error_datos));
            valido = false;
        }
        if (!confirmacionCheck.isChecked()) {
            confirmacionCheck.setError(getString(R.string.cancelar_cita_error_datos));
            valido = false;
        } else {
            confirmacionCheck.setError(null);
        }
        return valido;
    }
}