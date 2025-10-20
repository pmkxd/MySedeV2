# Guía de colores para MySede V2

La paleta cromática de MySede V2 está definida por los recursos de Android declarados en `res/values/colors.xml` y aplicados en `res/values/themes.xml`. Estas combinaciones construyen una identidad fresca y confiable basada en verdes teal que evocan bienestar y acompañamiento comunitario.

## Principios de color
- **Identidad institucional**: los verdes profundos (`md_theme_primary`, `md_theme_secondary`) transmiten equilibrio y confianza para la gestión de actividades.
- **Claridad operativa**: los contenedores claros (`md_theme_surface`, `md_theme_background`) privilegian la lectura y reducen la fatiga visual.
- **Accesibilidad**: la relación entre colores de fondo y de texto definidos en el tema Material 3 garantiza contrastes suficientes si se respetan los pares `onX` correspondientes.

## Paleta principal
| Uso en tema | Recurso | Hex | Aplicación recomendada |
| --- | --- | --- | --- |
| Primario | `md_theme_primary` | `#036B5D` | Botones principales, barra superior y acciones prioritarias. |
| Sobre primario | `md_theme_onPrimary` | `#FFFFFF` | Texto e íconos sobre elementos primarios. |
| Contenedor primario | `md_theme_primaryContainer` | `#9FF2E0` | Chips, tarjetas y fondos de componentes destacados. |
| Sobre contenedor primario | `md_theme_onPrimaryContainer` | `#005045` | Texto sobre contenedores primarios. |
| Secundario | `md_theme_secondary` | `#016B5D` | Acciones de soporte, pestañas y estados secundarios. |
| Sobre secundario | `md_theme_onSecondary` | `#FFFFFF` | Texto sobre elementos secundarios. |
| Contenedor secundario | `md_theme_secondaryContainer` | `#9FF2E1` | Badges de estado y elementos informativos. |
| Sobre contenedor secundario | `md_theme_onSecondaryContainer` | `#005046` | Texto sobre contenedores secundarios. |
| Fondo principal | `md_theme_surface` / `md_theme_background` | `#F5FBF7` | Fondos de pantalla y superficies de tarjetas. |
| Texto sobre fondo | `md_theme_onSurface` / `md_theme_onBackground` | `#171D1B` | Tipografía principal sobre superficies claras. |

## Colores de soporte
| Uso en tema | Recurso | Hex | Aplicación recomendada |
| --- | --- | --- | --- |
| Énfasis/Alertas | `md_theme_error` | `#BA1A1A` | Mensajes críticos, errores de validación y alertas urgentes. |
| Sobre alerta | `md_theme_onError` | `#FFFFFF` | Texto sobre avisos críticos. |
| Contenedor de alerta | `md_theme_errorContainer` | `#FFDAD6` | Fondos para avisos o banners de error. |
| Sobre contenedor de alerta | `md_theme_onErrorContainer` | `#93000A` | Texto sobre contenedores de error. |
| Variantes de superficie | `md_theme_surfaceContainer` a `md_theme_surfaceContainerHighest` | `#E9EFEC` … `#DEE4E1` | Jerarquía de tarjetas y paneles secundarios. |
| Contenido inverso | `md_theme_inverseSurface` / `md_theme_inverseOnSurface` | `#2B3230` / `#ECF2EF` | Elementos destacados en tarjetas oscuras o modo contraste. |
| Outline | `md_theme_outline` | `#6F7976` | Bordes sutiles y divisores. |

## Buenas prácticas
- Combina cada color de fondo con su respectivo color `on…` para asegurar contraste (por ejemplo, `md_theme_primary` con `md_theme_onPrimary`).
- Mantén el color primario para acciones críticas del flujo (crear actividad, registrar cita) y reserva el secundario para operaciones complementarias.
- Utiliza los contenedores (`*_Container`) para crear niveles de énfasis sin saturar con tonos puros.
- Para estados hover o focus aumenta el contraste usando las variantes `_mediumContrast` o `_highContrast` disponibles en `colors.xml`.
- Cuando necesites un esquema alterno (por ejemplo, mosaicos oscuros), emplea los pares `inverse` para conservar legibilidad.

Esta guía debe utilizarse como referencia al crear componentes reutilizables y diseñar nuevas vistas, garantizando coherencia con el tema de Material 3 configurado en la aplicación.