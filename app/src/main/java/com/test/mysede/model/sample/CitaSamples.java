package com.test.mysede.model.sample;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;
import com.test.mysede.model.Periodicidad;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
public final class CitaSamples {

    private CitaSamples() {
    }

    public static Cita proximaCitaDemostracion() {
        LocalDate hoy = LocalDate.now();
        Periodicidad periodicidad = Periodicidad.periodica(
                "Mensual",
                hoy.minusMonths(1),
                hoy.plusMonths(2)
        );
        Actividad actividad = new Actividad("Orientación nutricional", periodicidad);
        actividad.setDiasAvisoPrevio(2);
        Lugar lugar = new Lugar("Sede Central", Lugar.Tipo.OFICINA_DEL_CENTRO, 20);
        return new Cita(actividad, lugar, hoy.plusDays(5), LocalTime.of(10, 30));
    }

    public static List<Cita> citasSemanaDemostracion() {
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);

        Periodicidad cicloBimestral = Periodicidad.periodica(
                "Ciclo bimestral",
                lunes.minusMonths(1),
                lunes.plusMonths(2)
        );

        Actividad orientacion = new Actividad("Orientación nutricional", cicloBimestral);
        orientacion.setDiasAvisoPrevio(2);

        Actividad tallerMovimiento = new Actividad("Taller de movimiento", cicloBimestral);
        tallerMovimiento.setDiasAvisoPrevio(1);

        Actividad huerta = new Actividad("Huerta comunitaria", cicloBimestral);
        huerta.setDiasAvisoPrevio(3);

        Actividad asesoria = new Actividad("Asesoría legal comunitaria", cicloBimestral);
        asesoria.setDiasAvisoPrevio(4);

        Lugar sedeCentral = new Lugar("Sede Central", Lugar.Tipo.OFICINA_DEL_CENTRO, 20);
        Lugar salonComunitario = new Lugar("Salón Comunitario", Lugar.Tipo.LUGAR_DEL_TERRITORIO, 35);
        Lugar parqueBarrial = new Lugar("Parque barrial", Lugar.Tipo.LUGAR_DEL_TERRITORIO, 40);
        Lugar oficinaMovil = new Lugar("Oficina móvil", Lugar.Tipo.LUGAR_DEL_TERRITORIO, 15);

        List<Cita> citas = new ArrayList<>();
        citas.add(new Cita(orientacion, sedeCentral, lunes.plusDays(1), LocalTime.of(10, 30)));
        citas.add(new Cita(tallerMovimiento, salonComunitario, lunes.plusDays(2), LocalTime.of(15, 0)));
        citas.add(new Cita(huerta, parqueBarrial, lunes.plusDays(3), LocalTime.of(9, 0)));
        citas.add(new Cita(orientacion, sedeCentral, lunes.plusDays(4), LocalTime.of(12, 0)));
        citas.add(new Cita(asesoria, oficinaMovil, lunes.plusDays(5), LocalTime.of(11, 0)));

        return citas;
    }
}
