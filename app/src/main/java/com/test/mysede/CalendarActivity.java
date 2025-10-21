package com.test.mysede;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Clase principal que controla la actividad del calendario.
 * Permite visualizar las actividades en diferentes vistas: mes, semana o día.
 * La vista por defecto es la semanal.
 */
public class CalendarActivity extends Activity {
    // Contenedor principal donde se cargan las vistas (mes, semana o día)
    private FrameLayout calendarContainer;

    // Texto que muestra la fecha seleccionada actualmente
    private TextView txtFechaSeleccionada;

    // Botones para cambiar entre las vistas disponibles
    private Button btnMes, btnSemana, btnDia;

    // Fecha actualmente seleccionada por el usuario
    private Calendar fechaSeleccionada = Calendar.getInstance();

    // Inflador utilizado para cargar layouts dinámicamente
    private LayoutInflater inflater;

    /**
     * Método principal del ciclo de vida de la actividad.
     * Se ejecuta al crear la actividad y configura los elementos de la interfaz.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Inicialización del inflador y del contenedor principal
        inflater = LayoutInflater.from(this);
        calendarContainer = findViewById(R.id.calendarContainer);

        // Referencias a los elementos de la interfaz
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada);
        btnMes = findViewById(R.id.btnMes);
        btnSemana = findViewById(R.id.btnSemana);
        btnDia = findViewById(R.id.btnDia);

        // Se muestra por defecto la vista de semana
        mostrarVistaSemana();

        // Listeners para cambiar entre vistas
        btnMes.setOnClickListener(v -> mostrarVistaMes());
        btnSemana.setOnClickListener(v -> mostrarVistaSemana());
        btnDia.setOnClickListener(v -> mostrarVistaDia());
    }

    /**
     * Muestra la vista del calendario mensual.
     * Utiliza un CalendarView nativo de Android para seleccionar una fecha.
     */
    private void mostrarVistaMes() {
        // Limpiar cualquier vista anterior
        calendarContainer.removeAllViews();

        // Inflar la vista de calendario mensual
        CalendarView viewMonth = (CalendarView) inflater.inflate(R.layout.view_calendar_month, calendarContainer, false);

        // Detectar cambios de fecha seleccionada en el calendario
        viewMonth.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada.set(year, month, dayOfMonth);
            actualizarTextoFecha();
        });

        // Agregar la vista al contenedor
        calendarContainer.addView(viewMonth);

        // Actualizar el texto con la fecha seleccionada
        actualizarTextoFecha();
    }

    /**
     * Muestra la vista de la semana actual.
     * Presenta los días de la semana comenzando en lunes.
     */
    private void mostrarVistaSemana() {
        // Limpiar cualquier vista anterior
        calendarContainer.removeAllViews();

        // Inflar el layout correspondiente a la vista semanal
        LinearLayout viewWeek = (LinearLayout) inflater.inflate(R.layout.view_calendar_week, calendarContainer, false);

        // Contenedor de los días de la semana
        LinearLayout weekDaysContainer = viewWeek.findViewById(R.id.weekDaysContainer);
        weekDaysContainer.removeAllViews();

        // Se crea un clon del calendario para recorrer los días
        Calendar c = (Calendar) fechaSeleccionada.clone();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Semana comienza en lunes

        // Formato del día a mostrar
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM", new Locale("es", "ES"));

        // Mostrar los 7 días de la semana
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(this);
            tv.setText("• " + sdf.format(c.getTime()));
            tv.setPadding(4, 8, 4, 8);
            weekDaysContainer.addView(tv);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Agregar la vista al contenedor
        calendarContainer.addView(viewWeek);
        actualizarTextoFecha();
    }

    /**
     * Muestra la vista diaria.
     * Presenta las actividades asociadas al día seleccionado.
     */
    private void mostrarVistaDia() {
        // Limpiar cualquier vista anterior
        calendarContainer.removeAllViews();

        // Inflar la vista correspondiente al día
        LinearLayout viewDay = (LinearLayout) inflater.inflate(R.layout.view_calendar_day, calendarContainer, false);

        // Referencia al texto que muestra las actividades del día
        TextView txtDiaContenido = viewDay.findViewById(R.id.txtDiaContenido);

        // Formato amigable de la fecha
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
        txtDiaContenido.setText("Mostrando actividades del " + sdf.format(fechaSeleccionada.getTime()));

        // Agregar la vista al contenedor
        calendarContainer.addView(viewDay);
        actualizarTextoFecha();
    }

    /**
     * Actualiza el texto superior con la fecha actual seleccionada.
     */
    private void actualizarTextoFecha() {
        SimpleDateFormat formato = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        txtFechaSeleccionada.setText("Fecha actual: " + formato.format(fechaSeleccionada.getTime()));
    }
}
