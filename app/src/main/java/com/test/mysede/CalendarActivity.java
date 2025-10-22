package com.test.mysede;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {

    private LinearLayout contenedorDias;
    private TextView txtMesAnio, txtTituloActividades;
    private ImageButton btnCambiarMes;
    private ListView listActividades;
    private Button btnSemana;

    private Calendar fechaActual = Calendar.getInstance();
    private int diaSeleccionado = -1;
    private ArrayList<TextView> listaDias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        contenedorDias = findViewById(R.id.contenedorDias);
        txtMesAnio = findViewById(R.id.txtMesAnio);
        txtTituloActividades = findViewById(R.id.txtTituloActividades);
        listActividades = findViewById(R.id.listActividades);
        btnCambiarMes = findViewById(R.id.btnCambiarMes);
        btnSemana = findViewById(R.id.btnSemana);

        mostrarSemanaActual();

        btnCambiarMes.setOnClickListener(v -> abrirSelectorMes());
        btnSemana.setOnClickListener(v -> mostrarSemanaActual());
    }

    private void mostrarSemanaActual() {
        contenedorDias.removeAllViews();
        listaDias.clear();

        Calendar calendar = (Calendar) fechaActual.clone();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat sdfMes = new SimpleDateFormat("MMM, yyyy", new Locale("es", "ES"));
        txtMesAnio.setText(sdfMes.format(fechaActual.getTime()));

        Calendar hoy = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            int diaMes = calendar.get(Calendar.DAY_OF_MONTH);
            String diaSemana = new SimpleDateFormat("E", new Locale("es", "ES"))
                    .format(calendar.getTime()).substring(0, 1).toUpperCase();

            // contenedor de un día
            LinearLayout diaItem = new LinearLayout(this);
            diaItem.setOrientation(LinearLayout.VERTICAL);
            diaItem.setGravity(Gravity.CENTER);
            diaItem.setPadding(16, 8, 16, 8);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            diaItem.setLayoutParams(params);

            // nombre del día
            TextView tvDiaSemana = new TextView(this);
            tvDiaSemana.setText(diaSemana);
            tvDiaSemana.setTextColor(Color.parseColor("#666666"));
            tvDiaSemana.setGravity(Gravity.CENTER);

            // número del día
            TextView tvDiaNumero = new TextView(this);
            tvDiaNumero.setText(String.valueOf(diaMes));
            tvDiaNumero.setGravity(Gravity.CENTER);
            tvDiaNumero.setTextSize(18f);
            tvDiaNumero.setPadding(0, 10, 0, 10);

            // resalta el día actual
            if (calendar.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                    && calendar.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR)) {
                resaltarDia(tvDiaNumero);
                diaSeleccionado = i;
            }

            int index = i;
            diaItem.setOnClickListener(v -> seleccionarDia(index));

            diaItem.addView(tvDiaSemana);
            diaItem.addView(tvDiaNumero);
            contenedorDias.addView(diaItem);
            listaDias.add(tvDiaNumero);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        mostrarActividadesDia(fechaActual.get(Calendar.DAY_OF_WEEK) - 2);
    }

    private void seleccionarDia(int index) {
        for (TextView dia : listaDias) {
            dia.setBackground(null);
            dia.setTextColor(Color.parseColor("#333333"));
        }

        resaltarDia(listaDias.get(index));
        diaSeleccionado = index;
        mostrarActividadesDia(index);
    }

    private void resaltarDia(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_dia_seleccionado);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(20, 10, 20, 10);
    }

    private void abrirSelectorMes() {
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            fechaActual.set(Calendar.YEAR, year);
            fechaActual.set(Calendar.MONTH, month);
            fechaActual.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mostrarSemanaActual();
        }, fechaActual.get(Calendar.YEAR),
                fechaActual.get(Calendar.MONTH),
                fechaActual.get(Calendar.DAY_OF_MONTH));

        dp.show();
    }

    private void mostrarActividadesDia(int diaIndex) {
        ArrayList<String> lista = new ArrayList<>();

        Calendar c = (Calendar) fechaActual.clone();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.add(Calendar.DAY_OF_MONTH, diaIndex);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
        String fecha = sdf.format(c.getTime());

        txtTituloActividades.setText("Actividades del " + fecha);

        // ejemplo de actividades
        if (fecha.equals("29/10/2025")) {
            lista.add("• Taller de Programación IoT - 10:00 a 12:00");
            lista.add("• Reunión de docentes - 15:00");
            lista.add("• Actividad comunitaria - 18:00");
        } else {
            lista.add("• No hay actividades registradas para esta fecha");
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        listActividades.setAdapter(adapter);
    }
}
