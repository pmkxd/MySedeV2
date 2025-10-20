# Guía de diseño para vistas de gestión de actividades

Esta guía sirve como referencia para construir las pantallas de la aplicación siguiendo el modelo de dominio descrito en `README.md`.

## Principios generales
- **Consistencia**: reutilizar componentes para listas, formularios y tarjetas, manteniendo estilos homogéneos.
- **Visibilidad del estado**: mostrar claramente el estado de cada actividad (vigente, finalizada, con cupo lleno) y de sus citas (pendientes, en curso, ejecutadas).
- **Prevención de errores**: validar datos clave (fechas dentro de la periodicidad, cupos disponibles) antes de enviar formularios.
- **Accesibilidad**: usar contrastes suficientes, textos descriptivos y soporte para lectores de pantalla.

## Arquitectura de vistas
1. **Home / Dashboard**
    - Resumen de actividades programadas para el día y alertas próximas.
    - Acceso rápido a crear nueva actividad o registrar cita.

2. **Listado de actividades**
    - Tabla o tarjetas con filtros por proyecto, tipo, periodicidad y socio comunitario.
    - Indicadores visuales del número de citas, cupos ocupados y días de aviso configurados.
    - Acciones principales: ver detalle, editar actividad, crear cita.

3. **Detalle de actividad**
    - Mostrar información general (proyecto, tipos, oferentes, socio, adjuntos).
    - Sección de citas ordenadas cronológicamente con estado y botón para editar/cancelar.
    - Banner con el próximo aviso calculado (`calcularMomentoAviso`).

4. **Formulario de actividad**
    - Paso 1: datos generales (nombre, proyecto, tipo, cupo, socio comunitario).
    - Paso 2: periodicidad (selector puntual/periódica, fechas de inicio/fin).
    - Paso 3: configuración de avisos (`diasAvisoPrevio`) y adjuntos.
    - Validaciones en línea para evitar combinaciones inválidas (por ejemplo, actividad puntual con más de una fecha).

5. **Formulario de cita**
    - Selección de lugar disponible con cupo compatible.
    - Selección de fecha y hora dentro del rango de periodicidad.
    - Visualización del resumen del aviso previo calculado al confirmar.

6. **Gestión de beneficiarios**
    - Listado y buscador de beneficiarios asociados al socio comunitario.
    - Capacidad para agregar en lote (`agregarBeneficiarios`).

## Estados vacíos y errores
- Mensajes claros cuando no existan actividades o citas programadas.
- Indicaciones específicas cuando se infringen validaciones del modelo (por ejemplo, "La fecha de la cita no puede ser anterior al inicio").

## Navegación y jerarquía
- Breadcrumbs para ubicarse entre listado, detalle y edición.
- Acciones primarias visibles (botones destacados) y secundarias agrupadas en menús.

## Integración con alertas
- Programar un job o servicio que recorra las citas y consulte `calcularMomentoAviso` para generar notificaciones.
- Visualizar alertas pendientes en el dashboard y permitir confirmar su envío.

## Diseño responsive
- Distribución en una columna en dispositivos móviles y dos columnas en escritorio para detalle de actividad.
- Tarjetas con información esencial para listados en pantallas pequeñas.

## Identidad visual
- Utilizar la paleta definida en `guia-colores.md` para mantener coherencia cromática entre módulos.
- Priorizar el color primario para acciones principales y el secundario para elementos de soporte.
- Incorporar estados de foco/hover accesibles siguiendo las recomendaciones de contraste del documento de colores.

## Próximos pasos
- Definir componentes reutilizables (botones, inputs, tarjetas) en una biblioteca de UI.
- Elaborar prototipos de baja fidelidad (wireframes) para validar el flujo con usuarios antes de implementar.