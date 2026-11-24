# Estado del Proyecto Backend — HU-06 (Asignación Académica) y M-11 (Construcción de Horario)

> Documento de contexto generado en la fase de inicialización.
> **SÓLO LECTURA**: no debe modificarse después de su creación.

- **Fecha de análisis:** 2026-11-07
- **Rama de trabajo:** `backend-camilo` (prohibido crear ramas nuevas)
- **Módulo analizado:** únicamente `backend/` (Frontend NO se tocó ni se inspeccionó)

---

## 1. Estado de Git

### Rama actual
- `backend-camilo`, al día con `origin/backend-camilo` (0 commits de diferencia).
- HEAD local: `11b0734 fix: se corrigen algunas integraciones entre las migraciones Flyway y el mapeo de Hibernate`.

### Cambios locales que NO se deben perder
Cambios en stage (previos, del usuario):
- `backend/src/main/java/co/edu/ieruralyarumito/backend/controller/AuthController.java` (sólo una línea en blanco al final).
- `backend/src/main/java/co/edu/ieruralyarumito/backend/dto/LoginResponse.java` (sólo una línea en blanco al final).

Cambio sin stage:
- `.gitignore` (agrega `/_docs_referencia` y `/.agents-skills/*`).

### Sincronización con el compañero
- `origin/juan-carlos-backend` HEAD = `b14163a`.
- **Ya está completamente fusionado** en `backend-camilo` (el merge-base es exactamente `b14163a`; `git log backend-camilo..origin/juan-carlos-backend` está vacío).
- **No se requiere merge ni stash.** No se descartó ni movió ningún cambio.

### Remotos relevantes
`origin/backend-camilo`, `origin/juan-carlos-backend`, `origin/develop`, `origin/integracion`, `origin/master`, entre otras.

---

## 2. Stack y configuración

| Elemento | Valor |
|---|---|
| Java | 21 (`java.version` en pom; runtime 21.0.12) |
| Spring Boot | 4.1.1 |
| Build | Maven 3.9.9 |
| Persistencia | Spring Data JPA + Hibernate ORM 7.4.5 |
| Migraciones | Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`) |
| Base de datos | PostgreSQL 16 |
| Seguridad | Spring Security + JWT (`jjwt 0.12.6`), stateless |
| Validación | Jakarta Validation |
| BD en desarrollo | `docker-compose.yml` (PostgreSQL local, puerto 5432, db `yarumito`) |

`application.yaml` relevante:
- `spring.jpa.hibernate.ddl-auto: validate`
- `spring.jpa.open-in-view: false`
- `spring.flyway.locations: classpath:db/migration`
- `spring.flyway.baseline-on-migrate: false`
- CORS permitido para `http://localhost:5173` y `http://127.0.0.1:5173`.
- Todos los endpoints requieren autenticación salvo `POST /api/v1/auth/login`.

### Verificación ejecutada
- `mvn -q -DskipTests compile` → **OK**.
- `mvn -q test -Dtest=BackendApplicationTests` (Testcontainers PostgreSQL 16) → **OK**.
  - Flyway aplicó correctamente las **26 migraciones** (V1–V26).
  - Hibernate `validate` pasó sin errores.
  - 15 repositorios JPA detectados.
- Conclusión: el esquema y el mapeo actual arrancan; los problemas listados abajo son de **lógica/operación**, no de arranque.

---

## 3. Estructura de paquetes

Paquete base: `co.edu.ieruralyarumito.backend`

```
config/        SecurityConfig, JwtAuthenticationFilter, JwtProperties,
               RestAuthenticationEntryPoint, UsuarioInicialData, DemoDataSeed
controller/    16 controladores REST (catálogos, docentes, auth, grupos, etc.)
dto/           Requests/Responses por recurso + DTOs de asignación académica
entity/        18 entidades JPA + enums/
exception/     Excepciones de negocio + GlobalExceptionHandler
repository/    15 repositorios Spring Data
service/       16 servicios de negocio
specification/ Specifications de filtros (Area, Asignatura, Docente)
```

### Patrón dominante (a replicar en HU-06 y M-11)
- **Controller:** `@RestController`, `@RequestMapping("/api/v1/<recurso>")`, `ResponseEntity`, `@Valid`.
- **Service:** `@Service`, inyección por constructor, `@Transactional` en escrituras.
- **Repository:** `JpaRepository<Entidad, UUID>`; consultas derivadas con `_Id` (ej. `findByAnioEscolar_IdAndCodigo`).
- **DTOs:** clases planas con getters/setters (sin Lombok, aunque Lombok está en el pom).
- **Errores:** `RecursoNoEncontradoException` (404), `RecursoDuplicadoException` (409), `RelacionAcademicaInvalidaException` / `IllegalArgumentException` (400), manejados en `GlobalExceptionHandler` con cuerpo `{"mensaje": "..."}`.
- **Respuestas:** `POST` → 201; `GET/PUT` → 200; `DELETE` → 204.
- **Listados:** algunos usan `Page<...>` con `Pageable` (ej. `GrupoController`).

---

## 4. Inventario Entidad ↔ Tabla

| Entidad | Tabla / Esquema | Repositorio | Service | Controller |
|---|---|---|---|---|
| Usuario | `accesopersonas.usuario` | Sí | AuthService/JwtService | AuthController |
| Docente | `accesopersonas.docente` | Sí | DocenteService | DocenteController |
| TituloProfesional | `accesopersonas.titulo_profesional` | Sí | Sí | Sí |
| Idoneidad | `accesopersonas.idoneidad` | Sí | Sí | Sí |
| Area | `catalogoacademico.area` | Sí | Sí | Sí |
| Asignatura | `catalogoacademico.asignatura` | Sí | Sí | Sí |
| AnioEscolar | `catalogoacademico.anio_escolar` | Sí | Sí | Sí |
| Sede | `catalogoacademico.sede` | Sí | Sí | Sí |
| Grado | `catalogoacademico.grado` | Sí | Sí | Sí |
| Grupo | `catalogoacademico.grupo` | Sí | Sí | Sí |
| PlanEstudios | `catalogoacademico.planestudios` | Sí | Sí | Sí |
| Turno | `tiempoyespacio.turno` | Sí | Sí | Sí |
| FranjaHoraria | `tiempoyespacio.franjahoraria` | Sí | Sí | Sí |
| Aula | `tiempoyespacio.aula` | Sí | Sí | Sí |
| **AsignacionAcademica** | `asignacionyhorario.asignacionacademica` | Parcial | Parcial | **NO** |
| **Horario** | `asignacionyhorario.horario` | **NO** | **NO** | **NO** |
| **ClaseProgramada** | `asignacionyhorario.claseprogramada` | **NO** | **NO** | **NO** |
| **ActividadInstitucional** | `asignacionyhorario.actividadinstitucional` | **NO** | **NO** | **NO** |
| **AsignacionActividad** | `asignacionyhorario.asignacionactividad` | **NO** | **NO** | **NO** |

Sin entidad JPA (existen sólo en SQL): `controlyauditoria.restricciondocente`, `parametroinstitucional`, `conflicto`, `registroauditoria`.

---

## 5. Migraciones Flyway existentes (V1–V26)

| Versión | Contenido |
|---|---|
| V1 | Crea esquemas: `accesopersonas`, `catalogoacademico`, `tiempoyespacio`, `asignacionyhorario`, `controlyauditoria` |
| V2 | `usuario` |
| V3 | `area` |
| V4 | `docente` |
| V5 | `titulo_profesional` |
| V6 | `asignatura` |
| V7 | `idoneidad` |
| V8 | `anio_escolar` |
| V9 | `sede` |
| V10 | `grado` |
| V11 | `aula` |
| V12 | `grupo` |
| V13 | `turno` |
| V14 | `planestudios` |
| V15 | `franjahoraria` |
| V16 | `asignacionacademica` |
| V17 | `horario` |
| V18 | `claseprogramada` |
| V19 | `actividadinstitucional` |
| V20 | `asignacionactividad` |
| V21 | `restricciondocente` |
| V22 | `parametroinstitucional` |
| V23 | `conflicto` |
| V24 | `registroauditoria` |
| V25 | `grado.nombre` |
| V26 | `titulo_profesional.archivo_soporte` → `varchar(100)` |

**Próxima versión disponible: `V27`.** Prohibido modificar cualquier `.sql` existente.

---

## 6. Estado de HU-06 — Asignación Académica

### Lo que ya existe
- Entidad `AsignacionAcademica` con: `id`, `anioEscolar`, `docente`, `grupo`, `asignatura`, `horasSemanales`, `horasExtras`, `estado` (`BORRADOR`/`CONFIRMADA`), `observacion`, `asignadaPor`.
- Enum `EstadoAsignacionAcademica`.
- `AsignacionAcademicaRepository` (parcial).
- `AsignacionAcademicaService` con un único método `generateResumenAsignacionAcademica(anioEscolarId, grupoId)`.
- DTOs: `CrearAsignacionAcademicaRequest`, `ResumenAsignacionAcademicaResponse`, `AsignacionAcademicaFiltrosResponse`, `FiltrosAsigancionAcademicaResponse` (typo en el nombre), `GrupoOpcionResponse`, `AnioLectivoOpcionResponse`.
- Tabla `asignacionyhorario.asignacionacademica` (V16).

### Lo que falta / está incompleto
1. **No existe `AsignacionAcademicaController`**: el servicio no está expuesto por REST.
2. **CRUD inexistente**: no hay crear, listar, consultar, editar ni cambiar estado.
3. **Entidad incompleta**: faltan getters/setters de `anioEscolar`, `docente`, `grupo`, `asignatura` y `asignadaPor`; no tiene campo `fechaAsignacion`.
4. **Repositorio insuficiente**: extiende `Repository` (no `JpaRepository`), por lo que no tiene `save`, `findById`, `delete`. La consulta derivada `findByAnioEscolarAndGrupo(UUID, UUID)` no sigue la convención `_Id` (debería ser `findByAnioEscolar_IdAndGrupo_Id`); arranca, pero su semántica en ejecución debe verificarse.
5. **DTO de creación inutilizable**: `CrearAsignacionAcademicaRequest` **no tiene getters ni setters**, por lo que Jackson no podrá deserializar el body. Además incluye `tipoIdoneidad` (no es columna de la tabla) y `observacion` con `@NotNull` (probablemente debe ser opcional).
6. **Bug en el resumen**: `AsignacionAcademicaService` pasa `horasRequeridas` en el argumento `horasAsignadas` del response, y calcula el estado comparando el plan de estudios contra las horas esperadas del grado, sin contar las asignaciones reales.
7. **`PlanEstudiosRepository.findByAnioEscolarAndGrado(UUID, UUID)`** también debería usar `findByAnioEscolar_IdAndGrado_Id`.

### Brecha de esquema crítica para HU-06
- `V16` define `fecha_asignacion timestamp not null` **sin default** y la entidad no la mapea.
  → Cualquier `INSERT` vía JPA fallará con violación de `NOT NULL`.
  → Solución permitida: **nueva migración V27** (por ejemplo, `DEFAULT now()` o columna mapeada en la entidad), nunca editar V16.

---

## 7. Estado de M-11 — Construcción de Horario

### Lo que ya existe
- Entidades: `Horario`, `ClaseProgramada`, `ActividadInstitucional`, `AsignacionActividad`.
- Enums: `EstadoHorario` (`BORRADOR`/`PUBLICADO`/`ARCHIVADO`), `DiasSemana` (LUNES–VIERNES), `TipoActividadInstitucional`.
- Tablas V17–V20.

### Lo que falta
1. **No hay repositorios** para `Horario`, `ClaseProgramada`, `ActividadInstitucional` ni `AsignacionActividad`.
2. **No hay servicios ni controladores** para M-11.
3. **No hay DTOs** de horario/clase programada/actividad.
4. **No hay validaciones de conflictos** (choque docente/grupo/aula, horas extra, bloques consecutivos, disponibilidad, capacidad/tipo de aula, etc.), aunque la tabla `controlyauditoria.conflicto` (V23) ya contempla los tipos.
5. **Entidades incompletas**:
   - `Horario`: faltan getters y setters de `anioEscolarId`, `sedeId`, `creadoPor`; no hay setter de `fechaPublicacion`.
   - `ClaseProgramada`: getters/setters completos, pero los nombres `horarioId`, `asignacionAcademicaId`, `franjaHorariaId`, `aulaId` contienen entidades (no UUID).
   - `AsignacionActividad`: completa.
   - `ActividadInstitucional`: completa.

### Brechas de esquema relevantes para M-11
- `V18` no tiene restricciones únicas para evitar duplicados de programación (mismo horario + día + franja + aula, o + docente/grupo). La validación tendrá que hacerse en servicio y/o con nueva migración.
- `V17` `fecha_publicacion` es nullable; la entidad la inicializa en `LocalDateTime.now()` (aceptable, pero conviene revisar semántica: no debería fijarse al crear un borrador).
- `V20` `justificacion` es nullable en SQL, pero la entidad la exige `nullable = false`.
- `V23` `conflicto.asignacion_id` referencia `asignacionactividad`; semánticamente parece que debería referenciar `asignacionacademica` (revisar antes de usarla).

---

## 8. Inconsistencias de esquema detectadas (requieren migración NUEVA)

> Ninguna de estas se corrige editando migraciones existentes.

| # | Archivo | Problema | Impacto |
|---|---|---|---|
| 1 | `V16` | `fecha_asignacion` NOT NULL sin default y sin mapeo JPA | **Crítico**: todo INSERT de asignación falla |
| 2 | `V12` | FK `grupo.sede_id` referencia `catalogoacademico.anio_escolar(id)` en vez de `catalogoacademico.sede(id)` | **Crítico**: crear/actualizar grupo con sede falla si el UUID no existe como año escolar |
| 3 | `V11` | CHECK de `aula.tipo` usa `'SALA_SITEMAS'` (typo) y `'CANCHA'`; el enum usa `SALA_SISTEMAS` | Insertar aula tipo `SALA_SISTEMAS` viola el CHECK |
| 4 | `V6` | CHECK de `asignatura.tipo_aula_requ` usa `'SALA SISTEMAS'` (con espacio) y `'PATIO'`; el enum usa `SALA_SISTEMAS`/`CANCHA` | Desalineación catálogo ↔ enum |
| 5 | `V15` | CHECK `franja.numero between 1 and 6`; la entidad documenta `0` para descansos | No se pueden registrar descansos con número 0 |
| 6 | `V18` | Sin UNIQUE para evitar choques/duplicados de programación | Requiere validación en servicio |
| 7 | `V16` | Sin UNIQUE `(anio_escolar_id, grupo_id, asignatura_id)` | Permite asignaciones duplicadas |
| 8 | `V10`+`V25` | `grado.nombre NOT NULL` agregado sin default | Falla en BD con filas previas (en BD nueva no hay filas) |
| 9 | `V4` | `area_nombramiento_id NOT NULL` sin FK declarada | Integridad no garantizada |
| 10 | `V23` | `conflicto.asignacion_id` apunta a `asignacionactividad` | Posible FK incorrecta |

Además, entidad vs SQL (no bloquean el arranque porque `validate` no compara nullability):
- `Asignatura.area` es `optional = false` pero `asignatura.area_id` es nullable.
- `Aula.sede` es `optional = false` pero `aula.sede_id` es nullable.
- `Grupo.sede` es `optional = false` pero `grupo.sede_id` es nullable.

---

## 9. Endpoints REST existentes (referencia de estilo)

- `/api/v1/auth` (login público, usuario autenticado)
- `/api/v1/areas`, `/api/v1/asignaturas`, `/api/v1/anios-escolares`, `/api/v1/sedes`, `/api/v1/grados`, `/api/v1/grupos`, `/api/v1/planes-estudios`
- `/api/v1/docentes`, `/api/v1/titulos-profesionales`, `/api/v1/idoneidades`
- `/api/v1/turnos`, `/api/v1/franjas-horarias`, `/api/v1/aulas`
- Catálogos: `/api/v1/catalogos/areas`, `/api/v1/catalogos/asignaturas`, `/api/v1/catalogos/escalafon`
- **No existen** endpoints de asignación académica ni de horario.

---

## 10. Reglas de trabajo para las siguientes tareas

1. Trabajar **sólo** sobre `backend-camilo`; no crear ramas.
2. **No modificar** archivos `.sql` existentes; toda corrección de esquema va en una migración nueva (`V27__...sql` en adelante).
3. **No tocar** el Frontend ni leerlo.
4. No sobrescribir código existente salvo para completar HU-06 / M-11.
5. Preservar los cambios en stage de `AuthController.java` y `LoginResponse.java`.
6. Seguir el patrón Controller → Service → Repository → DTO del proyecto.
7. Al agregar endpoints, recordar que Spring Security exige autenticación por defecto.
8. Mantener este documento sin modificaciones.

---

## 11. Resumen ejecutivo

- Repositorio sincronizado; la rama del compañero ya está integrada; no hubo conflictos ni pérdida de cambios.
- El backend **compila y arranca** con las 26 migraciones actuales.
- **HU-06 está a medio construir**: entidad, tabla, repositorio parcial y un método de resumen; faltan controller, CRUD, accessors, DTO funcional y hay un bloqueo por `fecha_asignacion`.
- **M-11 está prácticamente sin construir en Java**: sólo existen entidades y tablas; faltan repositorios, servicios, controladores, DTOs y validaciones de conflictos.
- Hay **dos bugs de esquema críticos** (`fecha_asignacion` y FK de `grupo.sede_id`) que deben resolverse con migraciones nuevas.

---

# ACTUALIZACIÓN 2026-11-23 — Contexto de Producción (módulo Control y Salida → Publicación)

> Sección agregada por la tarea de diagnóstico solicitada. El contenido original de arriba se conserva sin cambios; esta sección refleja el estado real de `master` tras la sincronización.

## A. Sincronización Git ejecutada

- `git status` inicial: árbol limpio en `backend-camilo`.
- `git fetch --all --prune`: `origin/master` avanzó de `e51b6d9` a `b60f6d7`.
- `master` local actualizado con `git pull origin master` (HEAD actual: `b60f6d7 Merge pull request #39 from daluz0221/develop`).
- `git merge master` sobre `backend-camilo`: **fast-forward sin conflictos**. La rama quedó alineada exactamente con `master` (no se creó ninguna rama).
- No se modificó ningún archivo de código fuente ni de frontend en esta fase.

## B. Diagnóstico del botón "Publicar horario" (solo lectura)

Módulo de navegación: **Control y salida → Publicación** (`frontend/src/content/dashboard.ts`).
Ruta visual: `/dashboard/publicacion` (`frontend/src/App.tsx`).

### B.1 Condición real de habilitación/deshabilitación
Archivo: `frontend/src/components/organisms/PublicationLog/PublicationLog.tsx` (líneas 24-26).

```tsx
<Button variant="primary" size="sm" disabled>
  {published ? publicationCopy.published : publicationCopy.publish}
</Button>
```

- El botón **no evalúa ninguna condición de negocio**: está **deshabilitado de forma literal (`disabled` hardcodeado)** y **no tiene `onClick`**.
- El texto `logNote` lo confirma: *"La publicación desde esta pantalla se habilitará cuando el servidor la soporte."* (`frontend/src/content/publication.ts`, línea 57).
- Por tanto, el reporte de los compañeros es correcto: el botón siempre está deshabilitado, incluso con un horario válido y sin errores.
- Condición *implícita* que debería usarse al implementar (según el copy y la vista ya construida):
  - `view.hasSchedule === true` (existe al menos un `horarioId`).
  - `view.readiness !== 'blocked'` (sin conflictos `ERROR` pendientes). Las advertencias **no bloquean** (`warningsDisclaimer`).
  - `view.status !== 'PUBLICADO'` (evitar re-publicar).
  - El cálculo de `status` ya existe en `buildPublicationView`: es `PUBLICADO` sólo si **todos** los tableros con `horarioId` están `PUBLICADO`.

### B.2 Contrato que el frontend espera consumir actualmente
- **No existe llamada HTTP de publicación.** No hay función en `frontend/src/api/publicacionHorario.ts` ni en `frontend/src/api/horarios.ts` que haga `POST/PATCH/PUT` para publicar.
- El único consumo actual de la pestaña es de solo lectura:
  - `GET /api/horarios/grupos/{grupoId}` → `HorarioGrupoResponse` (incluye `horarioId`, `versionHorario`, `estadoHorario`).
  - `GET /api/v1/conflictos/horario/{horarioId}/pendientes` → conflictos pendientes.
- **Ruta/método/Request/Response esperados: no están definidos en el frontend actual.** Cualquier endpoint nuevo requiere también agregar la función de API y el `onClick` en el frontend (fase posterior; no se tocó ahora).
- Propuesta de contrato a implementar en la siguiente fase, coherente con el prefijo real de M-11 (`/api/horarios`, no `/api/v1/horarios`):
  - `POST /api/horarios/{horarioId}/publicar` sin body.
  - Respuesta sugerida `200 OK`:
    ```json
    {
      "horarioId": "uuid",
      "estado": "PUBLICADO",
      "fechaPublicacion": "2026-11-23T20:00:00",
      "version": 1
    }
    ```
  - Alternativa equivalente: `PATCH /api/horarios/{horarioId}/estado` con `{ "estado": "PUBLICADO" }`.
  - El endpoint debe validar en servidor que no haya conflictos `ERROR` pendientes y que el horario esté en `BORRADOR`.

## C. Estado del enum y del esquema PostgreSQL

- **`PUBLICADO` YA EXISTE** en Java: `backend/src/main/java/co/edu/ieruralyarumito/backend/entity/enums/EstadoHorario.java` (`BORRADOR`, `PUBLICADO`, `ARCHIVADO`).
- **`PUBLICADO` YA EXISTE** en PostgreSQL: `V17__create_horario_table.sql` líneas 7-8:
  ```sql
  estado varchar(20) not null check (estado in ('BORRADOR','PUBLICADO','ARCHIVADO'))
  ```
- No hay ninguna migración posterior (V34–V37) que altere ese `CHECK`.
- **Conclusión: NO se requiere migración nueva para el estado `PUBLICADO`.**
- La entidad `Horario` ya tiene `setEstado(...)` y `setFechaPublicacion(...)`; al crear un horario se fuerza `BORRADOR` y `fechaPublicacion = null` (`HorarioService.crearHorario`, líneas 1085-1086).

## D. Brecha de backend confirmada

- `HorarioController` sólo expone `GET /api/horarios/grupos/{grupoId}`.
- No existe controlador, servicio ni DTO de publicación. Prueba local con token:
  - `POST /api/horarios/{id}/publicar` → **404**.
  - `PATCH /api/v1/horarios/{id}/publicar` → **404**.
- Ya existe lógica parcial reutilizable:
  - `EstadoHorario.PUBLICADO` y la regla `validarHorarioEditable` que protege horarios publicados/archivados (`HorarioService`, líneas 794-805).
  - Repositorio `HorarioRepository` y consulta de conflictos `ConflictoService.listarPendientes(...)`.
- Falta: endpoint, transición `BORRADOR → PUBLICADO`, `fechaPublicacion = now()`, validación de conflictos `ERROR` pendientes, auditoría (`PUBLICAR`) y respuesta DTO.

## E. Despliegue local de validación (2026-11-23)

- Comando usado: `docker compose --profile app up -d --build`.
- Servicios activos:
  | Servicio | Contenedor | URL local |
  |---|---|---|
  | Frontend (Vite) | `proyecto_frontend` | http://localhost:5173 |
  | Backend (Spring Boot) | `proyecto_backend` | http://localhost:8080 |
  | PostgreSQL 16 | `proyecto_postgres` | `localhost:5432`, DB `yarumito` |
- Credenciales de login local (definidas en `.env`, **no** en `application.yaml`):
  - Usuario: `ejemplo@yarumito.edu.co`
  - Contraseña: `contrasenia-prueba`
  - Endpoint: `POST /api/v1/auth/login` con `{"username":"...","password":"..."}`.
- Verificaciones: frontend `200`, backend `healthy`, login `200`, `GET /api/v1/anios-escolares` y `/api/v1/grupos` responden `200`.
- **Incidente local resuelto:** el volumen Postgres previo (creado 2026-10-28) tenía *checksum mismatch* de Flyway en **V32 y V33** (producción editó esas migraciones). Se respaldó el volumen en
  `/tmp/opencode/yarumito-postgres_data-backup-20261123.tar.gz` y se recreó con `docker compose --profile app down -v`.
- Se ejecutó `docker compose --profile seed run --rm seed`: creó 6 áreas y 15 docentes.
- **Limitación de datos demo:** el seed **no crea años escolares, grupos, asignaciones ni horarios**. Para validar visualmente el botón hay que crear primero un año escolar y al menos un grupo desde la UI (o cargar datos). Sin ellos, Publicación muestra *"No hay años escolares registrados"*.
- Nota: en la rama remota existe `origin/fix/flyway-checksum-v32-v33`, aún no integrada; es relevante para entornos que conserven el volumen antiguo.

## F. Resumen ejecutivo de esta actualización

1. Git sincronizado con producción, sin conflictos y sin ramas nuevas.
2. El botón "Publicar horario" está **deshabilitado por código**, no por reglas de negocio.
3. El frontend **no tiene contrato de publicación definido**; sólo lecturas.
4. `PUBLICADO` **ya existe** en enum Java y `CHECK` de PostgreSQL: **no requiere migración**.
5. Falta todo el backend de publicación (endpoint + transición + validaciones + auditoría).
6. Servicios locales levantados y navegables; falta sembrar año/grupos para ver la pantalla con datos.
