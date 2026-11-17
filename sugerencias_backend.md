# Sugerencias para el backend

## Auditoría

La pantalla de auditoría solo consulta. No crea registros. Lo que aparece depende de lo que `RegistroAuditoriaService` recibe desde asignación académica y desde el horario.

### Quién puede verla

Los dos endpoints son de solo lectura y exigen sesión:

- `GET /api/v1/registros-auditoria/entidad/{entidad}/{idEntidad}`
- `GET /api/v1/registros-auditoria/usuario/{usuarioId}`

Spring Security pide un JWT válido en cualquier ruta distinta del login. Además, el controlador solo deja pasar el rol `RECTOR` o `ROLE_RECTOR`.

| Situación | Respuesta |
|---|---|
| Sin sesión | 401 |
| Sesión de otro rol, por ejemplo docente | 403 |
| Rector autenticado | 200 y la lista, vacía si no hay registros |

El identificador de usuario es el de la cuenta (`usuario`), no el del docente.

### Qué sí se registra

El registro se escribe en la misma transacción, y solo si el cambio de negocio se guardó. El usuario es la cuenta autenticada. La IP se envía siempre vacía, así que hoy queda en null.

**Asignación académica** (`AsignacionAcademica`)

- Crear la asignación: `CREATE`. No hay estado anterior.
- Confirmarla, de `BORRADOR` a `CONFIRMADA`: `UPDATE`, con el estado anterior y el nuevo.

La instantánea guarda `id`, `anioEscolarId`, `grupoId`, `docenteId`, `asignaturaId`, `estado` y `horasSemanales`.

**Clase programada** (`ClaseProgramada`)

- Crear el bloque: `CREATE`.
- Cambiar día, franja o aula: `UPDATE`.
- Eliminar el bloque: `DELETE`. El estado posterior queda vacío.

La instantánea guarda `id`, `horarioId`, `asignacionAcademicaId`, `dia`, `franjaHorariaId`, `aulaId` y `esHoraExtra`.

### Qué no se registra

Estas acciones no llaman al servicio de auditoría, así que no pueden aparecer en la pantalla:

- Crear, editar o cambiar el estado de un docente.
- Títulos profesionales e idoneidades.
- Sedes, aulas, turnos, franjas, grados, áreas y asignaturas.
- Año escolar, grupos y plan de estudios.
- Restricciones docentes.
- Actividades y proyectos.
- Conflictos y su resolución.
- Informes, PDF y Excel.
- Inicio de sesión.
- Publicar un horario. La acción `PUBLICAR` existe como constante, pero ningún servicio la usa.

Tampoco hay un listado general de auditoría. Solo se puede consultar por el id de una entidad o por el id de una cuenta. Por eso el front arma el historial de un grupo pidiendo, una por una, las asignaciones y las clases de ese grupo.

## Grupos del mismo grado con horarios distintos

601 y 602, ambos de Sexto, tienen las mismas materias y pueden armar horarios diferentes.

Las materias y las horas requeridas salen del plan de estudios, único por año, grado y asignatura. La asignación del docente se guarda por grupo: la unicidad es año + grupo + asignatura. Al asignar un docente en un grupo, el backend copia esa asignación a los demás grupos activos del mismo año, grado y sede. La copia lleva el mismo docente, las mismas horas, el mismo estado y la misma observación. Cada copia tiene su propio id, así que las clases y las horas pendientes de 601 no descuentan las de 602.

Esa copia también corre al listar las asignaciones de un grupo y al abrir su horario. Por eso una asignación que ya existía solo en 601 aparece en 602 la próxima vez que se consulta ese grupo, con todas sus horas por ubicar.

Cada bloque de la rejilla pertenece a la asignación de ese grupo. Al abrir Construcción de horario, la consulta `findByHorarioAndGrupo` devuelve solo las clases de 601 o solo las de 602.

El documento `horario` sigue siendo uno por año y sede. Los dos grupos lo comparten, y por eso usan las mismas franjas del grado. Lo que cambia entre ellos son los bloques: día, franja y aula. Un grupo no puede ocupar la misma franja dos veces. Si el docente o el aula ya están usados en esa franja por el otro grupo, el backend rechaza el bloque. La materia sigue disponible para otra franja.

Publicar ese horario cierra la edición de todos los grupos de la sede, porque el estado vive en el documento compartido. Separar la publicación por grupo sería otro cambio de backend. El plan de estudios sigue siendo el del grado.

## Asignación curricular: solo se puede confirmar

Al guardar el docente, la asignación curricular nace en `BORRADOR`. Desde ahí la pantalla solo ofrece **Confirmar**. Confirmarla la pasa a `CONFIRMADA` y ahí ya no queda ninguna acción, salvo ver el historial. Si el año lectivo está cerrado, la pantalla entera queda en consulta.

El backend acompaña ese límite. `AsignacionAcademicaController` solo expone:

- `POST /api/asignaciones`, que crea la fila en `BORRADOR`.
- `PATCH /api/asignaciones/{id}/confirmar`, que solo acepta una fila en `BORRADOR`.

No hay endpoint para cambiar docente, horas u observación, ni para quitar la asignación. Los botones de editar y quitar de la tabla curricular no abren ningún formulario. La pestaña de actividades sí permite editar y quitar, pero esos cambios viven en el navegador y no se guardan como asignación curricular.

La copia entre grupos del mismo grado arrastra ese estado. Si 601 queda en borrador, 602 también, y confirmar una no reabre la otra.

Para poder corregirla haría falta, en el backend:

1. Un `PATCH` de la asignación en `BORRADOR` para docente, horas semanales y observación, con las mismas validaciones de alta: docente activo, idoneidad vigente, continuidad del docente en el grado y unicidad de año + grupo + asignatura. La copia debería actualizar también a los grupos hermanos que sigan en borrador.
2. Un `DELETE` limitado al `BORRADOR` y solo si esa asignación todavía no tiene clases programadas. La misma regla aplicaría a las copias de los otros grupos.
3. Dejar `CONFIRMADA` cerrada, o permitir una corrección aparte que vuelva a validar docente y horas y que rechace el cambio cuando ya existan bloques en la rejilla. Esos bloques apuntan a la asignación, así que cambiar el docente movería también las clases ya ubicadas y podría chocar con otro bloque del mismo docente.
