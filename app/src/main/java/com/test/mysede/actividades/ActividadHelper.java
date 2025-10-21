package com.test.mysede.actividades;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.Periodicidad;
import com.test.mysede.model.TipoActividad;
import com.test.mysede.model.Lugar;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.Proyecto;
import com.test.mysede.model.SocioComunitario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper para generar datos de prueba de actividades
 */
public class ActividadHelper {

    private static List<Actividad> actividadesPrueba;

    public static List<Actividad> obtenerActividadesPrueba() {
        if (actividadesPrueba == null) {
            actividadesPrueba = generarActividadesPrueba();
        }
        return actividadesPrueba;
    }

    private static List<Actividad> generarActividadesPrueba() {
        List<Actividad> actividades = new ArrayList<>();

        // Actividad 1: Taller Puntual
        Periodicidad periodicidad1 = Periodicidad.puntual(
                "Única vez",
                LocalDate.of(2025, 11, 15)
        );
        Actividad actividad1 = new Actividad("Taller de Primeros Auxilios", periodicidad1);
        actividad1.setProyecto(new Proyecto("Salud Comunitaria"));
        actividad1.setCupo(25);
        actividad1.setDiasAvisoPrevio(3);

        List<TipoActividad> tipos1 = new ArrayList<>();
        tipos1.add(new TipoActividad(
                "Taller",
                "Actividad práctica de aprendizaje",
                TipoActividad.Categoria.TALLER
        ));
        actividad1.setTiposActividad(tipos1);

        List<OferenteActividad> oferentes1 = new ArrayList<>();
        oferentes1.add(new OferenteActividad(
                "Cruz Roja",
                "Dr. Carlos Méndez",
                OferenteActividad.Institucion.UNIVERSIDAD
        ));
        actividad1.setOferentes(oferentes1);

        SocioComunitario socio1 = new SocioComunitario("Junta de Vecinos N°5");
        actividad1.setSocioComunitario(socio1);

        actividades.add(actividad1);

        // Actividad 2: Capacitación Periódica
        Periodicidad periodicidad2 = Periodicidad.periodica(
                "Mensual",
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2026, 2, 28)
        );
        Actividad actividad2 = new Actividad("Capacitación en Emprendimiento", periodicidad2);
        actividad2.setProyecto(new Proyecto("Desarrollo Económico Local"));
        actividad2.setCupo(30);
        actividad2.setDiasAvisoPrevio(5);

        List<TipoActividad> tipos2 = new ArrayList<>();
        tipos2.add(new TipoActividad(
                "Capacitación",
                "Formación continua",
                TipoActividad.Categoria.CAPACITACION
        ));
        actividad2.setTiposActividad(tipos2);

        List<OferenteActividad> oferentes2 = new ArrayList<>();
        oferentes2.add(new OferenteActividad(
                "INACAP",
                "Mg. Patricia Rojas",
                OferenteActividad.Institucion.IP
        ));
        actividad2.setOferentes(oferentes2);

        SocioComunitario socio2 = new SocioComunitario("Asociación de Microempresarios");
        actividad2.setSocioComunitario(socio2);

        actividades.add(actividad2);

        // Actividad 3: Charla Puntual
        Periodicidad periodicidad3 = Periodicidad.puntual(
                "Única vez",
                LocalDate.of(2025, 11, 20)
        );
        Actividad actividad3 = new Actividad("Charla Informativa sobre Derechos", periodicidad3);
        actividad3.setProyecto(new Proyecto("Fortalecimiento Comunitario"));
        actividad3.setCupo(50);
        actividad3.setDiasAvisoPrevio(7);

        List<TipoActividad> tipos3 = new ArrayList<>();
        tipos3.add(new TipoActividad(
                "Charla",
                "Presentación informativa",
                TipoActividad.Categoria.CHARLA
        ));
        actividad3.setTiposActividad(tipos3);

        List<OferenteActividad> oferentes3 = new ArrayList<>();
        oferentes3.add(new OferenteActividad(
                "Universidad de Los Lagos",
                "Abg. María Fernández",
                OferenteActividad.Institucion.UNIVERSIDAD
        ));
        actividad3.setOferentes(oferentes3);

        SocioComunitario socio3 = new SocioComunitario("Centro de Adultos Mayores");
        actividad3.setSocioComunitario(socio3);

        actividades.add(actividad3);

        // Actividad 4: Operativo Periódico
        Periodicidad periodicidad4 = Periodicidad.periodica(
                "Semanal",
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 12, 31)
        );
        Actividad actividad4 = new Actividad("Operativo de Salud Rural", periodicidad4);
        actividad4.setProyecto(new Proyecto("Salud en Territorio"));
        actividad4.setCupo(20);
        actividad4.setDiasAvisoPrevio(2);

        List<TipoActividad> tipos4 = new ArrayList<>();
        tipos4.add(new TipoActividad(
                "Operativo Rural",
                "Atención en terreno",
                TipoActividad.Categoria.OPERATIVO_RURAL
        ));
        actividad4.setTiposActividad(tipos4);

        List<OferenteActividad> oferentes4 = new ArrayList<>();
        oferentes4.add(new OferenteActividad(
                "Centro de Salud Alerce",
                "Enf. Juan Pérez",
                OferenteActividad.Institucion.CFT
        ));
        actividad4.setOferentes(oferentes4);

        SocioComunitario socio4 = new SocioComunitario("Comité de Salud Rural");
        actividad4.setSocioComunitario(socio4);

        actividades.add(actividad4);

        // Actividad 5: Diagnóstico Puntual
        Periodicidad periodicidad5 = Periodicidad.puntual(
                "Única vez",
                LocalDate.of(2025, 11, 25)
        );
        Actividad actividad5 = new Actividad("Diagnóstico Participativo Comunitario", periodicidad5);
        actividad5.setProyecto(new Proyecto("Planificación Territorial"));
        actividad5.setCupo(40);
        actividad5.setDiasAvisoPrevio(10);

        List<TipoActividad> tipos5 = new ArrayList<>();
        tipos5.add(new TipoActividad(
                "Diagnóstico",
                "Levantamiento de información",
                TipoActividad.Categoria.DIAGNOSTICO
        ));
        actividad5.setTiposActividad(tipos5);

        List<OferenteActividad> oferentes5 = new ArrayList<>();
        oferentes5.add(new OferenteActividad(
                "Universidad Austral",
                "Soc. Roberto Silva",
                OferenteActividad.Institucion.UNIVERSIDAD
        ));
        actividad5.setOferentes(oferentes5);

        SocioComunitario socio5 = new SocioComunitario("Mesa Territorial Alerce");
        actividad5.setSocioComunitario(socio5);

        actividades.add(actividad5);

        return actividades;
    }

    public static Actividad obtenerActividadPorIndice(int indice) {
        List<Actividad> actividades = obtenerActividadesPrueba();
        if (indice >= 0 && indice < actividades.size()) {
            return actividades.get(indice);
        }
        return null;
    }

    public static void agregarActividad(Actividad actividad) {
        if (actividadesPrueba == null) {
            actividadesPrueba = generarActividadesPrueba();
        }
        actividadesPrueba.add(actividad);
    }

    public static void actualizarActividad(int indice, Actividad actividad) {
        if (actividadesPrueba != null && indice >= 0 && indice < actividadesPrueba.size()) {
            actividadesPrueba.set(indice, actividad);
        }
    }

    public static void eliminarActividad(int indice) {
        if (actividadesPrueba != null && indice >= 0 && indice < actividadesPrueba.size()) {
            actividadesPrueba.remove(indice);
        }
    }
}