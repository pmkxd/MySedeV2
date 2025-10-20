# MySede V2 - Modelos de dominio

Este documento describe las entidades principales del dominio implementado en la aplicación **MySede V2** para gestionar actividades y citas dentro del centro integral.

## Visión general

El modelo cubre los siguientes requerimientos del sistema:

- Registrar actividades que agrupan citas y definen su periodicidad (puntual o periódica).
- Administrar la creación de citas asociadas a una actividad, validando lugar, fecha y hora.
- Restringir las actividades puntuales a una única cita y permitir múltiples citas para actividades periódicas.
- Asociar información contextual como proyecto, oferentes, tipos de actividad, archivos adjuntos y socio comunitario.
- Calcular el momento en que debe emitirse un aviso previo a la ejecución de una cita.

## Entidades principales

### Actividad (`Actividad`)
- Define el nombre, periodicidad, cupo, tipos de actividad y oferentes.
- Administra las citas asociadas mediante `crearCita`, aplicando las validaciones de periodicidad y tipo.
- Permite configurar `diasAvisoPrevio` para determinar cuándo se genera una alerta antes de la cita (`calcularMomentoAviso`).

### Cita (`Cita`)
- Representa la ocurrencia concreta de una actividad, especificando `Lugar`, fecha (`LocalDate`) y hora (`LocalTime`).
- Se crea únicamente a través de `Actividad.crearCita` para garantizar las reglas de negocio.

### Periodicidad (`Periodicidad`)
- Modela si la actividad es puntual o periódica mediante `Tipo`.
- Las fábricas estáticas (`puntual`, `periodica`) simplifican la creación aplicando las reglas de validación sobre fecha de inicio y fin.

### Lugar (`Lugar`)
- Describe el sitio donde se ejecuta la actividad. Puede ser una oficina del centro o un lugar del territorio.
- Opcionalmente incluye un `cupo` máximo.

### Socio comunitario y beneficiarios (`SocioComunitario`, `Beneficiario`)
- Permite asociar un socio comunitario y sus beneficiarios para contextualizar la actividad.
- `SocioComunitario` ofrece operaciones para agregar beneficiarios individuales o en lote.

### Oferente de actividad (`OferenteActividad`)
- Registra la institución y responsable que imparten la actividad.
- Admite distintos tipos de institución (`Institucion`).

### Tipo de actividad (`TipoActividad`)
- Clasifica las actividades según su categoría (capacitaciones, talleres, diagnósticos, etc.).
- Incluye nombre y descripción para mostrar información al usuario final.

### Archivo adjunto (`ArchivoAdjunto`)
- Referencia material complementario vinculado a la actividad (planificaciones, recursos, etc.).

### Proyecto (`Proyecto`)
- Identifica el proyecto al que pertenece la actividad, facilitando el agrupamiento en reportes.

## Flujo de gestión de citas
1. Crear la `Actividad` indicando su `Periodicidad`.
2. Configurar datos adicionales (tipos, oferentes, socio comunitario, etc.).
3. Registrar una o más `Cita` mediante `crearCita`, definiendo `Lugar`, fecha y hora.
4. Definir `diasAvisoPrevio` para habilitar alertas anticipadas.
5. Calcular el instante del aviso por cita con `calcularMomentoAviso`.

Estas entidades conforman la base para construir servicios y vistas que gestionen la agenda de actividades del centro integral.
