package com.test.mysede;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.mysede.model.Lugar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maneja la carga y las operaciones de calendario contra Cloud Firestore.
 */
final class CalendarRepository {

    interface LoadListener {
        void onSuccess(@NonNull List<CalendarUiCita> citas);

        void onError(@NonNull Exception exception);
    }

    interface UpdateListener {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }

    private static final String COLLECTION_CITAS = "citas";
    private static final String COLLECTION_ACTIVIDADES = "actividades";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    void loadAppointments(@NonNull LoadListener listener) {
        firestore.collection(COLLECTION_CITAS)
                .get()
                .addOnSuccessListener(result -> procesarCitas(result, listener))
                .addOnFailureListener(e -> listener.onError(e));
    }

    void reagendar(@NonNull CalendarUiCita cita,
                   @NonNull LocalDate nuevaFecha,
                   @NonNull LocalTime nuevaHora,
                   @NonNull UpdateListener listener) {
        String remoteId = cita.getRemoteId();
        if (remoteId == null) {
            listener.onError(new IllegalStateException("La cita no tiene ID remoto"));
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("fecha", nuevaFecha.format(DATE_FORMATTER));
        data.put("hora", nuevaHora.format(TIME_FORMATTER));
        firestore.collection(COLLECTION_CITAS)
                .document(remoteId)
                .update(data)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    void cancelar(@NonNull CalendarUiCita cita, @NonNull UpdateListener listener) {
        String remoteId = cita.getRemoteId();
        if (remoteId == null) {
            listener.onError(new IllegalStateException("La cita no tiene ID remoto"));
            return;
        }
        firestore.collection(COLLECTION_CITAS)
                .document(remoteId)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    private void procesarCitas(@NonNull QuerySnapshot snapshot, @NonNull LoadListener listener) {
        List<CalendarUiCita> citas = new ArrayList<>();
        Map<String, List<CalendarUiCita>> citasPorActividad = new HashMap<>();
        Set<String> actividadesPendientes = new HashSet<>();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            CalendarUiCita uiCita = mapearCita(document);
            if (uiCita == null) {
                continue;
            }
            citas.add(uiCita);
            if (uiCita.getActividadId() != null) {
                actividadesPendientes.add(uiCita.getActividadId());
                List<CalendarUiCita> asociadas = citasPorActividad.get(uiCita.getActividadId());
                if (asociadas == null) {
                    asociadas = new ArrayList<>();
                    citasPorActividad.put(uiCita.getActividadId(), asociadas);
                }
                asociadas.add(uiCita);
            }
        }

        if (actividadesPendientes.isEmpty()) {
            listener.onSuccess(ordenarPorFecha(citas));
            return;
        }

        List<Task<DocumentSnapshot>> tareas = new ArrayList<>();
        for (String actividadId : actividadesPendientes) {
            tareas.add(firestore.collection(COLLECTION_ACTIVIDADES).document(actividadId).get());
        }

        Tasks.whenAllComplete(tareas)
                .addOnSuccessListener(results -> {
                    for (Object result : results) {
                        if (!(result instanceof Task)) {
                            continue;
                        }
                        Task<?> task = (Task<?>) result;
                        if (!task.isSuccessful()) {
                            continue;
                        }
                        Object taskResult = task.getResult();
                        if (taskResult instanceof DocumentSnapshot) {
                            DocumentSnapshot document = (DocumentSnapshot) taskResult;
                            String actividadId = document.getId();
                            String nombre = document.getString("nombre");
                            List<CalendarUiCita> asociadas = citasPorActividad.get(actividadId);
                            if (asociadas != null) {
                                for (CalendarUiCita cita : asociadas) {
                                    cita.setActividadNombre(nombre);
                                }
                            }
                        }
                    }
                    listener.onSuccess(ordenarPorFecha(citas));
                })
                .addOnFailureListener(listener::onError);
    }

    @Nullable
    private CalendarUiCita mapearCita(@NonNull DocumentSnapshot document) {
        String remoteId = document.getId();
        String actividadId = document.getString("actividadId");
        String fechaStr = document.getString("fecha");
        String horaStr = document.getString("hora");
        Map<String, Object> lugarMap = document.get("lugar", Map.class);
        if (fechaStr == null || horaStr == null || lugarMap == null) {
            return null;
        }
        LocalDate fecha = LocalDate.parse(fechaStr, DATE_FORMATTER);
        LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        Lugar lugar = mapearLugar(lugarMap);
        if (lugar == null) {
            return null;
        }
        CalendarUiCita cita = new CalendarUiCita(remoteId, actividadId, null, lugar, fecha, hora, 60);
        if (document.contains("actividadNombre")) {
            cita.setActividadNombre(document.getString("actividadNombre"));
        }
        return cita;
    }

    @Nullable
    private Lugar mapearLugar(@NonNull Map<String, Object> data) {
        Object nombreObj = data.get("nombre");
        Object tipoObj = data.get("tipo");
        if (!(nombreObj instanceof String) || !(tipoObj instanceof String)) {
            return null;
        }
        String nombre = (String) nombreObj;
        String tipoStr = (String) tipoObj;
        Lugar.Tipo tipo;
        try {
            tipo = Lugar.Tipo.valueOf(tipoStr);
        } catch (IllegalArgumentException ex) {
            tipo = Lugar.Tipo.OFICINA_DEL_CENTRO;
        }
        Integer cupo = null;
        Object cupoObj = data.get("cupo");
        if (cupoObj instanceof Number) {
            cupo = ((Number) cupoObj).intValue();
        }
        Lugar lugar = new Lugar(nombre, tipo, cupo);
        Object id = data.get("id");
        if (id instanceof String) {
            lugar.setId((String) id);
        }
        return lugar;
    }

    private List<CalendarUiCita> ordenarPorFecha(List<CalendarUiCita> citas) {
        Collections.sort(citas, (a, b) -> {
            int compareFecha = a.getFecha().compareTo(b.getFecha());
            if (compareFecha != 0) {
                return compareFecha;
            }
            return a.getHora().compareTo(b.getHora());
        });
        return citas;
    }
}