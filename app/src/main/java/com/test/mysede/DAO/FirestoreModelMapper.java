package com.test.mysede.DAO;

import androidx.annotation.Nullable;

import com.test.mysede.model.Actividad;
import com.test.mysede.model.ArchivoAdjunto;
import com.test.mysede.model.Beneficiario;
import com.test.mysede.model.Cita;
import com.test.mysede.model.Lugar;
import com.test.mysede.model.OferenteActividad;
import com.test.mysede.model.Periodicidad;
import com.test.mysede.model.Proyecto;
import com.test.mysede.model.SocioComunitario;
import com.test.mysede.model.TipoActividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utilidad para convertir los modelos de dominio hacia y desde estructuras compatibles con Firestore.
 */
final class FirestoreModelMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    private FirestoreModelMapper() {
        // Solo utilidades.
    }

    static Map<String, Object> actividadToMap(Actividad actividad) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", actividad.getId());
        data.put("nombre", actividad.getNombre());
        data.put("cupo", actividad.getCupo());
        data.put("diasAvisoPrevio", actividad.getDiasAvisoPrevio());
        data.put("periodicidad", periodicidadToMap(actividad.getPeriodicidad()));
        data.put("proyecto", proyectoToMap(actividad.getProyecto()));
        data.put("tiposActividad", tiposActividadToList(actividad.getTiposActividad()));
        data.put("oferentes", oferentesToList(actividad.getOferentes()));
        data.put("socioComunitario", socioComunitarioToMap(actividad.getSocioComunitario()));
        data.put("archivosAdjuntos", archivosAdjuntosToList(actividad.getArchivosAdjuntos()));
        // Mientras no exista soporte para adjuntar archivos, se almacena como null
        //data.put("archivosAdjuntos", null);
        data.put("citas", citasToList(actividad.getCitas()));
        return data;
    }

    static Actividad actividadFromMap(Map<String, Object> data, String documentId) {
        if (data == null) {
            return null;
        }
        Map<String, Object> periodicidadData = castMap(data.get("periodicidad"));
        Periodicidad periodicidad = periodicidadFromMap(periodicidadData);
        String nombre = (String) data.get("nombre");
        Actividad actividad = new Actividad(nombre, Objects.requireNonNull(periodicidad));
        actividad.setId(documentId != null ? documentId : (String) data.get("id"));
        Object cupoValue = data.get("cupo");
        if (cupoValue != null) {
            actividad.setCupo(castInteger(cupoValue));
        } else {
            actividad.setCupo(null);
        }
        Object diasAvisoPrevio = data.get("diasAvisoPrevio");
        if (diasAvisoPrevio != null) {
            actividad.setDiasAvisoPrevio(castInteger(diasAvisoPrevio));
        }
        Map<String, Object> proyectoData = castMap(data.get("proyecto"));
        Proyecto proyecto = proyectoFromMap(proyectoData);
        if (proyecto != null) {
            actividad.setProyecto(proyecto);
        }
        List<Map<String, Object>> tiposActividadData = castListOfMaps(data.get("tiposActividad"));
        if (tiposActividadData != null) {
            List<TipoActividad> tiposActividad = new ArrayList<>();
            for (Map<String, Object> tipoData : tiposActividadData) {
                TipoActividad tipo = tipoActividadFromMap(tipoData);
                if (tipo != null) {
                    tiposActividad.add(tipo);
                }
            }
            actividad.setTiposActividad(tiposActividad);
        }
        List<Map<String, Object>> oferentesData = castListOfMaps(data.get("oferentes"));
        if (oferentesData != null) {
            List<OferenteActividad> oferentes = new ArrayList<>();
            for (Map<String, Object> oferenteData : oferentesData) {
                OferenteActividad oferente = oferenteFromMap(oferenteData);
                if (oferente != null) {
                    oferentes.add(oferente);
                }
            }
            actividad.setOferentes(oferentes);
        }
        Map<String, Object> socioData = castMap(data.get("socioComunitario"));
        SocioComunitario socioComunitario = socioComunitarioFromMap(socioData);
        if (socioComunitario != null) {
            actividad.setSocioComunitario(socioComunitario);
        }
        List<Map<String, Object>> archivosData = castListOfMaps(data.get("archivosAdjuntos"));
        if (archivosData != null) {
            List<ArchivoAdjunto> archivosAdjuntos = new ArrayList<>();
            for (Map<String, Object> archivoData : archivosData) {
                ArchivoAdjunto archivoAdjunto = archivoAdjuntoFromMap(archivoData);
                if (archivoAdjunto != null) {
                    archivosAdjuntos.add(archivoAdjunto);
                }
            }
            actividad.setArchivosAdjuntos(archivosAdjuntos);
        }
        List<Map<String, Object>> citasData = castListOfMaps(data.get("citas"));
        if (citasData != null) {
            actividad.limpiarCitas();
            for (Map<String, Object> citaData : citasData) {
                Cita cita = citaFromMap(citaData, actividad);
                if (cita != null) {
                    actividad.getCitas();
                }
            }
        }
        return actividad;
    }

    static Map<String, Object> archivoAdjuntoToMap(@Nullable ArchivoAdjunto archivoAdjunto) {
        if (archivoAdjunto == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", archivoAdjunto.getId());
        data.put("nombre", archivoAdjunto.getNombre());
        data.put("tipo", archivoAdjunto.getTipo());
        data.put("tamaño", archivoAdjunto.getTamaño());
        data.put("url", archivoAdjunto.getUrl());
        return data;
    }

    static ArchivoAdjunto archivoAdjuntoFromMap(@Nullable Map<String, Object> data) {
        if (data == null) return null;

        String nombre = (String) data.get("nombre");
        String tipo = (String) data.get("tipo");
        Long tamaño = (Long) data.get("tamaño");
        String url = (String) data.get("url");


        if (nombre == null || tipo == null || tamaño == null || url == null) {
            return null;
        }

        ArchivoAdjunto archivoAdjunto = new ArchivoAdjunto(nombre, tipo, tamaño, null, url);

        Object id = data.get("id");
        if (id instanceof String) archivoAdjunto.setId((String) id);

        return archivoAdjunto;
    }

    static Map<String, Object> beneficiarioToMap(Beneficiario beneficiario) {
        Map<String, Object> data = new HashMap<>();
        data.put("caracterizacion", beneficiario.getCaracterizacion());
        return data;
    }

    static Beneficiario beneficiarioFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String caracterizacion = (String) data.get("caracterizacion");
        return caracterizacion != null ? new Beneficiario(caracterizacion) : null;
    }

    static Map<String, Object> citaToMap(Cita cita) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", cita.getId());
        data.put("actividadId", cita.getActividad() != null ? cita.getActividad().getId() : null);
        data.put("lugar", lugarToMap(cita.getLugar()));
        data.put("fecha", cita.getFecha() != null ? cita.getFecha().format(DATE_FORMATTER) : null);
        data.put("hora", cita.getHora() != null ? cita.getHora().format(TIME_FORMATTER) : null);
        return data;
    }

    static Cita citaFromMap(@Nullable Map<String, Object> data, Actividad actividad) {
        if (data == null || actividad == null) {
            return null;
        }
        Map<String, Object> lugarData = castMap(data.get("lugar"));
        Lugar lugar = lugarFromMap(lugarData);
        String fechaStr = (String) data.get("fecha");
        String horaStr = (String) data.get("hora");
        if (lugar == null || fechaStr == null || horaStr == null) {
            return null;
        }
        LocalDate fecha = LocalDate.parse(fechaStr, DATE_FORMATTER);
        LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        Cita cita = actividad.crearCita(lugar, fecha, hora);
        Object id = data.get("id");
        if (id instanceof String) {
            cita.setId((String) id);
        }
        return cita;
    }

    static Map<String, Object> lugarToMap(@Nullable Lugar lugar) {
        if (lugar == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", lugar.getId());
        data.put("nombre", lugar.getNombre());
        data.put("tipo", lugar.getTipo() != null ? lugar.getTipo().name() : null);
        data.put("cupo", lugar.getCupo().orElse(null));
        return data;
    }

    static Lugar lugarFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String nombre = (String) data.get("nombre");
        String tipoStr = (String) data.get("tipo");
        if (nombre == null || tipoStr == null) {
            return null;
        }
        Lugar.Tipo tipo = Lugar.Tipo.valueOf(tipoStr);
        Integer cupo = castInteger(data.get("cupo"));
        Lugar lugar = new Lugar(nombre, tipo, cupo);
        Object id = data.get("id");
        if (id instanceof String) {
            lugar.setId((String) id);
        }
        return lugar;
    }

    static Map<String, Object> oferenteToMap(@Nullable OferenteActividad oferenteActividad) {
        if (oferenteActividad == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", oferenteActividad.getId());
        data.put("nombre", oferenteActividad.getNombre());
        data.put("docenteResponsable", oferenteActividad.getDocenteResponsable());
        data.put("institucion", oferenteActividad.getInstitucion() != null ? oferenteActividad.getInstitucion().name() : null);
        return data;
    }

    static OferenteActividad oferenteFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String nombre = (String) data.get("nombre");
        String docenteResponsable = (String) data.get("docenteResponsable");
        String institucionStr = (String) data.get("institucion");
        if (nombre == null || docenteResponsable == null || institucionStr == null) {
            return null;
        }
        OferenteActividad oferenteActividad = new OferenteActividad(nombre, docenteResponsable, OferenteActividad.Institucion.valueOf(institucionStr));
        Object id = data.get("id");
        if (id instanceof String) {
            oferenteActividad.setId((String) id);
        }
        return oferenteActividad;
    }

    static Map<String, Object> periodicidadToMap(@Nullable Periodicidad periodicidad) {
        if (periodicidad == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", periodicidad.getNombre());
        data.put("tipo", periodicidad.getTipo().name());
        data.put("fechaInicio", periodicidad.getFechaInicio().map(date -> date.format(DATE_FORMATTER)).orElse(null));
        data.put("fechaFin", periodicidad.getFechaFin().map(date -> date.format(DATE_FORMATTER)).orElse(null));
        return data;
    }

    static Periodicidad periodicidadFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String nombre = (String) data.get("nombre");
        String tipoStr = (String) data.get("tipo");
        if (nombre == null || tipoStr == null) {
            return null;
        }
        Periodicidad.Tipo tipo = Periodicidad.Tipo.valueOf(tipoStr);
        String fechaInicioStr = (String) data.get("fechaInicio");
        String fechaFinStr = (String) data.get("fechaFin");
        LocalDate fechaInicio = fechaInicioStr != null ? LocalDate.parse(fechaInicioStr, DATE_FORMATTER) : null;
        LocalDate fechaFin = fechaFinStr != null ? LocalDate.parse(fechaFinStr, DATE_FORMATTER) : null;
        if (tipo == Periodicidad.Tipo.PUNTUAL) {
            return Periodicidad.puntual(nombre, fechaInicio);
        }
        return Periodicidad.periodica(nombre, fechaInicio, fechaFin);
    }

    static Map<String, Object> proyectoToMap(@Nullable Proyecto proyecto) {
        if (proyecto == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", proyecto.getId());
        data.put("nombre", proyecto.getNombre());
        return data;
    }

    static Proyecto proyectoFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String nombre = (String) data.get("nombre");
        if (nombre == null) {
            return null;
        }
        Proyecto proyecto = new Proyecto(nombre);
        Object id = data.get("id");
        if (id instanceof String) {
            proyecto.setId((String) id);
        }
        return proyecto;
    }

    static Map<String, Object> socioComunitarioToMap(@Nullable SocioComunitario socioComunitario) {
        if (socioComunitario == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", socioComunitario.getId());
        data.put("nombre", socioComunitario.getNombre());
        List<Map<String, Object>> beneficiarios = new ArrayList<>();
        for (Beneficiario beneficiario : socioComunitario.getBeneficiarios()) {
            beneficiarios.add(beneficiarioToMap(beneficiario));
        }
        data.put("beneficiarios", beneficiarios);
        return data;
    }

    static SocioComunitario socioComunitarioFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String nombre = (String) data.get("nombre");
        if (nombre == null) {
            return null;
        }
        SocioComunitario socioComunitario = new SocioComunitario(nombre);
        Object id = data.get("id");
        if (id instanceof String) {
            socioComunitario.setId((String) id);
        }
        List<Map<String, Object>> beneficiariosData = castListOfMaps(data.get("beneficiarios"));
        if (beneficiariosData != null) {
            List<Beneficiario> beneficiarios = new ArrayList<>();
            for (Map<String, Object> beneficiarioData : beneficiariosData) {
                Beneficiario beneficiario = beneficiarioFromMap(beneficiarioData);
                if (beneficiario != null) {
                    beneficiarios.add(beneficiario);
                }
            }
            socioComunitario.agregarBeneficiarios(beneficiarios);
        }
        return socioComunitario;
    }

    static Map<String, Object> tipoActividadToMap(@Nullable TipoActividad tipoActividad) {
        if (tipoActividad == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", tipoActividad.getId());
        data.put("nombre", tipoActividad.getNombre());
        data.put("descripcion", tipoActividad.getDescripcion());
        data.put("categoria", tipoActividad.getCategoria() != null ? tipoActividad.getCategoria().name() : null);
        return data;
    }

    static TipoActividad tipoActividadFromMap(@Nullable Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String nombre = (String) data.get("nombre");
        String descripcion = (String) data.get("descripcion");
        String categoriaStr = (String) data.get("categoria");
        if (nombre == null || descripcion == null || categoriaStr == null) {
            return null;
        }
        TipoActividad tipoActividad = new TipoActividad(nombre, descripcion, TipoActividad.Categoria.valueOf(categoriaStr));
        Object id = data.get("id");
        if (id instanceof String) {
            tipoActividad.setId((String) id);
        }
        return tipoActividad;
    }

    private static List<Map<String, Object>> archivosAdjuntosToList(List<ArchivoAdjunto> archivosAdjuntos) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (archivosAdjuntos == null) {
            return list;
        }
        for (ArchivoAdjunto archivoAdjunto : archivosAdjuntos) {
            list.add(archivoAdjuntoToMap(archivoAdjunto));
        }
        return list;
    }

    private static List<Map<String, Object>> citasToList(List<Cita> citas) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (citas == null) {
            return list;
        }
        for (Cita cita : citas) {
            list.add(citaToMap(cita));
        }
        return list;
    }

    private static List<Map<String, Object>> oferentesToList(List<OferenteActividad> oferentes) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (oferentes == null) {
            return list;
        }
        for (OferenteActividad oferenteActividad : oferentes) {
            list.add(oferenteToMap(oferenteActividad));
        }
        return list;
    }

    private static List<Map<String, Object>> tiposActividadToList(List<TipoActividad> tiposActividad) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (tiposActividad == null) {
            return list;
        }
        for (TipoActividad tipoActividad : tiposActividad) {
            list.add(tipoActividadToMap(tipoActividad));
        }
        return list;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castListOfMaps(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Map<String, Object>> maps = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map) {
                    maps.add((Map<String, Object>) item);
                }
            }
            return maps;
        }
        return null;
    }

    private static int castInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return 0;
    }
}