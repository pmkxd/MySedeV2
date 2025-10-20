package com.test.mysede.model.sample;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;
import com.test.mysede.model.Periodicidad;

import java.time.LocalDate;
import java.time.LocalTime;

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
}
