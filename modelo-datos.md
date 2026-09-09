# Modelo de datos — Gestión de Horarios I.E.R. Yarumito

Documento de referencia del modelo conceptual: tablas, campos, unicidades y relaciones, agrupados por responsabilidad.

**Alcance acordado**

- Asignación manual del rector con validación de reglas
- Versionado por año escolar (borrador / publicado)
- Jerarquía Área → Asignatura → Plan de estudios
- Sin estudiantes (solo grupos)
- Aulas con validación de choque
- Media técnica como asignatura especial de 10.º y 11.º en contrajornada
- Idoneidad: títulos + decreto + tabla explícita
- Compensación con actividades institucionales
- Roles: rector y docente
- Auditoría de cambios

**Convenciones**

| Abreviatura | Significado |
|-------------|-------------|
| PK | Clave primaria |
| FK | Clave foránea |
| UK | Restricción de unicidad |
| N | Nullable (opcional) |

---

## 1. Acceso y personas

Responsabilidad: autenticación, perfil laboral del docente, títulos e idoneidad para dictar áreas/asignaturas.

### 1.1 `Usuario`

Cuenta de acceso al sistema. El rector no requiere perfil docente; el docente sí (relación 1:1).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| correo | String | UK, not null | Correo de login |
| contrasena_hash | String | not null | Hash de la contraseña |
| rol | Enum | not null | `RECTOR` \| `DOCENTE` |
| activo | Boolean | not null | Si puede iniciar sesión |
| ultimo_acceso | DateTime | N | Último login |

**Relaciones**

| Relación | Cardinalidad | Entidad | Notas |
|----------|--------------|---------|-------|
| perfil | 1:0..1 | Docente | Solo si el usuario es docente |
| crea | 1:N | Horario | `Horario.creado_por` |
| aprueba | 1:N | Idoneidad | `Idoneidad.aprobada_por` |
| aprueba | 1:N | RestriccionDocente | `RestriccionDocente.aprobada_por` |
| asigna | 1:N | AsignacionAcademica | `AsignacionAcademica.asignada_por` |
| genera | 1:N | RegistroAuditoria | Trazabilidad de acciones |

---

### 1.2 `Docente`

Perfil laboral y contractual. Aquí viven nombramiento, carga esperada y flags de media técnica.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| usuario_id | UUID | FK → Usuario, UK | Login asociado (1:1) |
| nombres | String | not null | Nombres |
| apellidos | String | not null | Apellidos |
| tipo_documento | String | not null | CC, CE, etc. |
| numero_documento | String | UK, not null | Documento de identidad |
| telefono | String | N | Contacto |
| correo_institucional | String | N | Correo institucional |
| tipo_vinculacion | Enum | not null | `PLANTA` \| `PROVISIONAL` \| `CONTRATO` |
| area_nombramiento_id | UUID | FK → Area | Área del decreto de nombramiento |
| numero_decreto | String | N | Número del decreto |
| fecha_decreto | Date | N | Fecha del decreto |
| escalafon | String | N | Escalafón docente |
| horas_semanales_contratadas | Int | not null, default 22 | Carga contractual semanal |
| max_horas_extra | Int | not null, default 10 | Tope de horas extras |
| es_exclusivo_media_tecnica | Boolean | not null, default false | Docente nombrado para la especialidad |
| estado | Enum | not null | `ACTIVO` \| `LICENCIA` \| `RETIRADO` |
| fecha_vinculacion | Date | N | Inicio de vinculación |

**Relaciones**

| Relación | Cardinalidad | Entidad | Notas |
|----------|--------------|---------|-------|
| usuario | N:1 | Usuario | Login |
| area_nombramiento | N:1 | Area | Área principal del decreto |
| posee | 1:N | TituloProfesional | Títulos que respaldan idoneidad |
| habilitado_por | 1:N | Idoneidad | Qué puede dictar |
| dicta | 1:N | AsignacionAcademica | Qué dicta este año |
| dirige | 1:N | Grupo | Director de grupo |
| restringe | 1:N | RestriccionDocente | Indisponibilidades |
| compensa | 1:N | AsignacionActividad | Proyectos que completan carga |

---

### 1.3 `TituloProfesional`

Títulos académicos del docente. Un docente puede tener varios.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| docente_id | UUID | FK → Docente | Docente titular |
| nivel | Enum | not null | `NORMALISTA` \| `LICENCIATURA` \| `PROFESIONAL` \| `ESPECIALIZACION` \| `MAESTRIA` \| `DOCTORADO` |
| nombre_titulo | String | not null | Nombre del título |
| institucion | String | N | Institución otorgante |
| anio_graduacion | Int | N | Año de graduación |
| archivo_soporte | String | N | Ruta/archivo del diploma |

**Relaciones**

| Relación | Cardinalidad | Entidad | Notas |
|----------|--------------|---------|-------|
| docente | N:1 | Docente | Dueño del título |
| soporta | 1:N | Idoneidad | `Idoneidad.titulo_soporte_id` |

---

### 1.4 `Idoneidad`

Habilitación del docente para un área y, opcionalmente, una asignatura. **Un docente tiene muchas idoneidades** (una por área/asignatura habilitada).

No confundir con asignación: idoneidad = *puede* dictar; asignación = *está dictando* este año.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| docente_id | UUID | FK → Docente | Docente habilitado |
| area_id | UUID | FK → Area | Área cubierta |
| asignatura_id | UUID | FK → Asignatura, N | Si es null, aplica a toda el área |
| tipo | Enum | not null | `PRINCIPAL` \| `AUTORIZADA` \| `EXCEPCIONAL` |
| titulo_soporte_id | UUID | FK → TituloProfesional, N | Título que respalda la habilitación |
| justificacion | Text | N | Obligatoria sobre todo en `EXCEPCIONAL` |
| vigente_desde | Date | not null | Inicio de vigencia |
| vigente_hasta | Date | N | Fin de vigencia (null = vigente) |
| aprobada_por | UUID | FK → Usuario, N | Rector que aprueba excepciones |

**Tipos**

| Valor | Significado |
|-------|-------------|
| PRINCIPAL | Área del decreto de nombramiento |
| AUTORIZADA | Por título o contexto institucional |
| EXCEPCIONAL | Aprobada por el rector (p. ej. colegio sin plaza de esa área) |

**Relaciones**

| Relación | Cardinalidad | Entidad | Notas |
|----------|--------------|---------|-------|
| docente | N:1 | Docente | Quién está habilitado |
| area | N:1 | Area | Área de la habilitación |
| asignatura | N:0..1 | Asignatura | Materia específica (opcional) |
| titulo_soporte | N:0..1 | TituloProfesional | Evidencia académica |
| aprobada_por | N:0..1 | Usuario | Quién autorizó |

---

## 2. Catálogo académico

Responsabilidad: estructura curricular versionada por año (áreas, asignaturas, grados, grupos, plan de estudios, sede).

### 2.1 `AnioEscolar`

Eje de versionado. Grados, docentes y horarios cambian cada año.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| anio | Int | not null | Año lectivo (ej. 2026) |
| fecha_inicio | Date | not null | Inicio del periodo |
| fecha_fin | Date | not null | Fin del periodo |
| estado | Enum | not null | `PLANEACION` \| `ACTIVO` \| `CERRADO` |
| es_actual | Boolean | not null | Marca el año en uso |

**Relaciones (padre de)**

`Grupo`, `PlanEstudios`, `AsignacionAcademica`, `Horario`, `ActividadInstitucional`, `RestriccionDocente`, `ParametroInstitucional` — todas 1:N.

---

### 2.2 `Sede`

Ubicación física. Hoy: sede principal; el modelo admite más.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| nombre | String | not null | Nombre de la sede |
| codigo | String | UK, not null | Código corto |
| direccion | String | N | Dirección |
| es_principal | Boolean | not null | Sede principal |

**Relaciones:** 1:N con `Grupo`, `Aula`, `Horario`.

---

### 2.3 `Area`

Área curricular (p. ej. Matemáticas, Ciencias naturales). Agrupa asignaturas y ancla el nombramiento del docente.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| nombre | String | not null | Nombre del área |
| codigo | String | UK, not null | Código |
| obligatoria | Boolean | not null | Área obligatoria (art. 23 Ley 115) |
| solo_media | Boolean | not null | Solo para 10.º–11.º (filosofía, económicas) |
| activa | Boolean | not null | Si está en uso |

**Relaciones**

| Relación | Cardinalidad | Entidad |
|----------|--------------|---------|
| contiene | 1:N | Asignatura |
| nombramiento | 1:N | Docente |
| cubre | 1:N | Idoneidad |

---

### 2.4 `Asignatura`

Materia concreta que aparece en el horario (Castellano, Biología, Media Técnica, etc.).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| area_id | UUID | FK → Area | Área a la que pertenece |
| nombre | String | not null | Nombre |
| codigo | String | UK, not null | Código |
| abreviatura | String | N | Abreviatura en grillas (CASTELLA, EDU. FÍSICA) |
| color_ui | String | N | Color para la interfaz |
| exige_idoneidad_estricta | Boolean | not null | Si false, cualquier docente puede (ética, artística) |
| es_media_tecnica | Boolean | not null | Asignatura de media técnica (contrajornada) |
| requiere_docente_exclusivo | Boolean | not null | Debe dictarla el docente de la especialidad |
| tipo_aula_requerida | Enum | N | `AULA` \| `LABORATORIO` \| `SALA_SISTEMAS` \| `CANCHA` |
| max_clases_consecutivas | Int | not null, default 2 | Tope de bloques seguidos de la misma materia |
| activa | Boolean | not null | Si está en uso |

**Relaciones:** N:1 `Area`; 1:N `PlanEstudios`, `AsignacionAcademica`, `Idoneidad`.

---

### 2.5 `Grado`

Nivel curricular (6.º–11.º), no el curso concreto.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| nivel | Int | not null | 6 … 11 |
| nombre | String | not null | Ej. "Sexto", "Undécimo" |
| es_media | Boolean | not null | true en 10 y 11 |
| prioridad_asignacion | Int | not null | Orden de armado (superiores primero) |
| horas_semanales_esperadas | Int | not null | 30 bachillerato / 37 con media técnica |

**Relaciones:** 1:N `Grupo`, `PlanEstudios`.

---

### 2.6 `Grupo`

Curso real del año (601, 702, 1102). La media técnica **no** crea grupos propios: son los mismos 10° / 1101 / 1102 en contrajornada.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| codigo | String | not null | 601, 1102, etc. |
| grado_id | UUID | FK → Grado | Nivel curricular |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| sede_id | UUID | FK → Sede | Sede |
| director_grupo_id | UUID | FK → Docente, N | Director de grupo |
| cantidad_estudiantes | Int | N | Cupo / matrícula (sin modelar estudiantes) |
| aula_fija_id | UUID | FK → Aula, N | Salón habitual |
| activo | Boolean | not null | Si el grupo está activo |

**Unicidad:** UK `(anio_escolar_id, codigo)`.

**Relaciones:** N:1 Grado, AnioEscolar, Sede, Docente (director), Aula; 1:N `AsignacionAcademica`.

---

### 2.7 `PlanEstudios`

Intensidad horaria semanal de cada asignatura en cada grado y año. Define la **demanda** que el rector debe cubrir. El `turno` indica si esas horas van en mañana o contrajornada (media técnica).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| grado_id | UUID | FK → Grado | Grado |
| asignatura_id | UUID | FK → Asignatura | Materia |
| horas_semanales | Int | not null | Intensidad semanal |
| turno_id | UUID | FK → Turno | Turno esperado (mañana / contrajornada) |
| observacion | Text | N | Notas |

**Unicidad:** UK `(anio_escolar_id, grado_id, asignatura_id)`.

**Relaciones:** N:1 AnioEscolar, Grado, Asignatura, Turno.  
Es la relación N:M **con atributos** entre Grado y Asignatura.

---

## 3. Tiempo y espacio

Responsabilidad: plantillas de jornada, bloques horarios y aulas físicas.

### 3.1 `Turno`

Plantilla de jornada (no pertenece al grupo). Ejemplos: Mañana (6:30–12:30) y Contrajornada (1:30–5:30).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| nombre | String | not null | Mañana \| Contrajornada |
| hora_inicio | Time | not null | Inicio del turno |
| hora_fin | Time | not null | Fin del turno |
| duracion_clase_minutos | Int | not null | 55 en mañana; puede ser otro en tarde |
| clases_por_dia | Int | not null | Ej. 6 en mañana |
| clases_antes_de_descanso | Int | not null | Ej. 3 |
| duracion_descanso_minutos | Int | not null | Ej. 30 |

**Relaciones:** 1:N `FranjaHoraria`, `PlanEstudios`.

---

### 3.2 `FranjaHoraria`

Bloque concreto de la rejilla (hora 1 = 6:30–7:25, etc.). Compartida por todo el colegio.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| turno_id | UUID | FK → Turno | Turno al que pertenece |
| numero | Int | not null | Número de bloque (1…6) |
| hora_inicio | Time | not null | Inicio |
| hora_fin | Time | not null | Fin |
| es_descanso | Boolean | not null | Si es el hueco de descanso |
| horas_academicas_equivalentes | Decimal | not null | Mañana ≈ 1; bloque tarde largo ≈ 4 |
| orden | Int | not null | Orden para secuencias y UI |

**Relaciones:** N:1 Turno; 1:N `ClaseProgramada`, `RestriccionDocente`.

> `horas_academicas_equivalentes` permite cuadrar la carga (p. ej. 12 h de media técnica en un bloque de 4 h académicas).

---

### 3.3 `Aula`

Espacio físico con validación de choque (dos clases no usan la misma aula a la misma hora).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| sede_id | UUID | FK → Sede | Sede |
| nombre | String | not null | Nombre / código del salón |
| tipo | Enum | not null | `AULA` \| `LABORATORIO` \| `SALA_SISTEMAS` \| `CANCHA` |
| capacidad | Int | N | Capacidad |
| activa | Boolean | not null | Si está disponible |

**Relaciones:** N:1 Sede; 1:N `ClaseProgramada`; opcionalmente 1:N como `aula_fija` de `Grupo`.

---

## 4. Asignación y horario

Responsabilidad: carga académica del docente, grilla semanal, compensación de horas y publicación del horario.

### 4.1 `AsignacionAcademica`

Decisión del rector: qué docente dicta qué asignatura a qué grupo, con cuántas horas. Es la vista que consulta el docente (“mis cursos y materias”).

Distinta de `ClaseProgramada`: la asignación es la **carga**; las clases son **cuándo y dónde** ocurre.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| docente_id | UUID | FK → Docente | Quién dicta |
| grupo_id | UUID | FK → Grupo | A qué curso |
| asignatura_id | UUID | FK → Asignatura | Qué materia |
| horas_semanales | Int | not null | Intensidad acordada |
| horas_extra | Int | not null, default 0 | Parte que cuenta como extra |
| estado | Enum | not null | `BORRADOR` \| `CONFIRMADA` |
| observacion | Text | N | Notas |
| asignada_por | UUID | FK → Usuario | Rector que asignó |
| fecha_asignacion | DateTime | not null | Momento de la asignación |

**Unicidad:** UK `(anio_escolar_id, grupo_id, asignatura_id)` — una materia de un grupo la dicta un solo docente.

**Relaciones:** N:1 AnioEscolar, Docente, Grupo, Asignatura, Usuario; 1:N `ClaseProgramada`.  
Relación ternaria Docente–Grupo–Asignatura materializada como entidad.

---

### 4.2 `Horario`

Cabecera versionada del calendario de un año/sede.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| sede_id | UUID | FK → Sede | Sede |
| version | Int | not null | Número de versión |
| nombre | String | not null | Nombre del escenario |
| estado | Enum | not null | `BORRADOR` \| `PUBLICADO` \| `ARCHIVADO` |
| fecha_publicacion | DateTime | N | Cuándo se publicó |
| creado_por | UUID | FK → Usuario | Autor |
| notas | Text | N | Comentarios |

**Unicidad sugerida:** UK `(anio_escolar_id, sede_id, version)`.

**Relaciones:** N:1 AnioEscolar, Sede, Usuario; 1:N `ClaseProgramada`, `Conflicto`.

---

### 4.3 `ClaseProgramada`

Cada casilla del horario: día + franja + aula, ligada a una asignación académica.

**No duplica** docente, grupo ni asignatura: los hereda de `AsignacionAcademica`.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| horario_id | UUID | FK → Horario | Versión del horario |
| asignacion_academica_id | UUID | FK → AsignacionAcademica | Carga que se materializa |
| dia | Enum | not null | `LUNES` … `VIERNES` |
| franja_horaria_id | UUID | FK → FranjaHoraria | Bloque horario |
| aula_id | UUID | FK → Aula | Espacio físico |
| es_hora_extra | Boolean | not null, default false | Si cuenta como extra |
| bloqueada | Boolean | not null, default false | Fijada por el rector (no mover) |

**Unicidades (anti-choque)**

| Unicidad | Regla |
|----------|-------|
| `(horario_id, grupo*, dia, franja_horaria_id)` | Un grupo no tiene dos clases a la vez |
| `(horario_id, docente*, dia, franja_horaria_id)` | Un docente no está en dos sitios a la vez |
| `(horario_id, aula_id, dia, franja_horaria_id)` | Un aula no se usa dos veces a la vez |

\* grupo y docente se obtienen vía `AsignacionAcademica`.

**Relaciones:** N:1 Horario, AsignacionAcademica, FranjaHoraria, Aula.

---

### 4.4 `ActividadInstitucional`

Proyecto o actividad con la que el rector **compensa** horas cuando el docente no llega a 22. No es una asignatura ni entra al plan del estudiante.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| nombre | String | not null | Nombre del proyecto/actividad |
| tipo | Enum | not null | Ver enum abajo |
| descripcion | Text | N | Detalle |
| horas_semanales | Int | not null | Horas de referencia del catálogo |

**Enum `tipo`**

| Valor | Uso |
|-------|-----|
| PROYECTO_PEDAGOGICO | Proyecto pedagógico institucional |
| ACTIVIDAD_INSTITUCIONAL | Actividad institucional general |
| DIRECCION_DE_GRUPO | Carga por dirección de grupo (si aplica) |
| COMITE | Participación en comités |
| OTRO | Otros |

**Relaciones:** N:1 AnioEscolar; 1:N `AsignacionActividad`.

---

### 4.5 `AsignacionActividad`

Asigna una actividad institucional a un docente concreto (horas que suman a su carga).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| docente_id | UUID | FK → Docente | Docente compensado |
| actividad_id | UUID | FK → ActividadInstitucional | Actividad |
| horas_semanales | Int | not null | Horas asignadas a ese docente |
| justificacion | Text | N | Por qué se asigna |

**Relaciones:** N:1 Docente, ActividadInstitucional.  
N:M Docente ↔ ActividadInstitucional con atributos.

**Carga total del docente** ≈ suma de horas de clases (ponderadas por `FranjaHoraria.horas_academicas_equivalentes`) + suma de `AsignacionActividad.horas_semanales`.

---

## 5. Control y auditoría

Responsabilidad: restricciones de disponibilidad, parámetros configurables, conflictos de validación y trazabilidad.

### 5.1 `RestriccionDocente`

Franjas en las que el docente no puede (o prefiere no) dictar, con justificación.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| docente_id | UUID | FK → Docente | Docente |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| dia | Enum | not null | `LUNES` … `VIERNES` |
| franja_horaria_id | UUID | FK → FranjaHoraria | Bloque afectado |
| tipo | Enum | not null | `NO_DISPONIBLE` \| `PREFERENTE` |
| motivo | Text | not null | Justificación |
| documento_soporte | String | N | Archivo / evidencia |
| aprobada_por | UUID | FK → Usuario, N | Rector que aprueba |

**Tipos:** `NO_DISPONIBLE` genera error al programar; `PREFERENTE` genera advertencia.

**Relaciones:** N:1 Docente, AnioEscolar, FranjaHoraria, Usuario.

---

### 5.2 `ParametroInstitucional`

Constantes de negocio por año (evita hardcodear 22, 10, 55, etc.).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| anio_escolar_id | UUID | FK → AnioEscolar | Año |
| clave | String | not null | Clave estable (ej. `MAX_HORAS_EXTRA`) |
| valor | String | not null | Valor serializado |
| tipo_dato | String | not null | `INT` \| `DECIMAL` \| `BOOL` \| `STRING` |
| descripcion | Text | N | Explicación |

**Unicidad sugerida:** UK `(anio_escolar_id, clave)`.

**Ejemplos de claves**

| Clave | Valor típico |
|-------|--------------|
| HORAS_DOCENTE_SEMANALES | 22 |
| MAX_HORAS_EXTRA | 10 |
| DURACION_CLASE_MIN | 55 |
| CLASES_POR_DIA | 6 |
| MAX_CLASES_CONSECUTIVAS | 2 |
| HORAS_ESTUDIANTE_BACHILLERATO | 30 |
| HORAS_ESTUDIANTE_MEDIA_TECNICA | 37 |

---

### 5.3 `Conflicto`

Resultado materializado de las validaciones del horario. Permite guardar borradores “rotos”, listar problemas y bloquear publicación si hay errores.

Las unicidades de BD cubren choques simples; este modelo cubre **reglas calculadas** (carga, consecutivas, idoneidad, turno de media técnica, plan incompleto, etc.).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| horario_id | UUID | FK → Horario | Versión afectada |
| tipo | Enum | not null | Tipo de regla incumplida |
| severidad | Enum | not null | `ERROR` \| `ADVERTENCIA` |
| mensaje | Text | not null | Descripción legible |
| resuelto | Boolean | not null, default false | Si ya se corrigió |
| fecha_deteccion | DateTime | not null | Momento de detección |

**Campos recomendados adicionales (implementación)**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| asignacion_id | UUID, N | Asignación relacionada |
| docente_id | UUID, N | Docente relacionado |
| grupo_id | UUID, N | Grupo relacionado |
| clases_implicadas | N:M → ClaseProgramada | Casillas en conflicto |

**Enum `tipo`**

| Valor | Severidad típica |
|-------|------------------|
| CHOQUE_DOCENTE | ERROR |
| CHOQUE_GRUPO | ERROR |
| CHOQUE_AULA | ERROR |
| EXCEDE_HORAS_CONTRACTUALES | ERROR / ADVERTENCIA |
| EXCEDE_HORAS_EXTRA | ERROR |
| CLASES_CONSECUTIVAS_EXCEDIDAS | ERROR |
| DOCENTE_SIN_IDONEIDAD | ERROR |
| DOCENTE_NO_DISPONIBLE | ERROR |
| TURNO_INCORRECTO | ERROR |
| PLAN_ESTUDIOS_INCOMPLETO | ADVERTENCIA / ERROR |
| HORAS_ASIGNACION_NO_CUADRAN | ADVERTENCIA |
| AULA_TIPO_INCORRECTO | ADVERTENCIA |
| CAPACIDAD_AULA | ADVERTENCIA |
| CARGA_AREA_NOMBRAMIENTO | ADVERTENCIA |

**Flujo:** editar asignación/clase → motor de validación → insertar/actualizar `Conflicto` → UI lista → publicar solo si no hay `ERROR` abiertos.

**Relaciones:** N:1 Horario.

---

### 5.4 `RegistroAuditoria`

Trazabilidad de quién cambió qué y cuándo.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| id | UUID | PK | Identificador |
| usuario_id | UUID | FK → Usuario | Quién actuó |
| accion | String | not null | CREATE, UPDATE, DELETE, PUBLICAR, etc. |
| entidad | String | not null | Nombre de la tabla/entidad |
| id_entidad | UUID | not null | Id del registro afectado |
| datos_antes | JSON | N | Estado anterior |
| datos_despues | JSON | N | Estado nuevo |
| fecha | DateTime | not null | Momento |
| ip | String | N | IP de origen |

**Relaciones:** N:1 Usuario.

---

## 6. Mapa de relaciones entre dominios

```text
[Acceso y personas]
  Usuario ──1:1── Docente
  Docente ──1:N── TituloProfesional
  Docente ──1:N── Idoneidad ──N:1── Area / Asignatura

[Catálogo académico]
  Area ──1:N── Asignatura
  Grado + Asignatura + AnioEscolar + Turno ──► PlanEstudios
  Grado + AnioEscolar + Sede ──► Grupo

[Tiempo y espacio]
  Turno ──1:N── FranjaHoraria
  Sede ──1:N── Aula

[Asignación y horario]
  Docente + Grupo + Asignatura + Anio ──► AsignacionAcademica
  AsignacionAcademica + Franja + Aula + Horario ──► ClaseProgramada
  ActividadInstitucional + Docente ──► AsignacionActividad

[Control]
  Docente + Franja + Anio ──► RestriccionDocente
  Anio ──► ParametroInstitucional
  Horario ──► Conflicto
  Usuario ──► RegistroAuditoria
```

---

## 6. Unicidades críticas (resumen)

| Entidad | Unicidad | Regla de negocio |
|---------|----------|------------------|
| AsignacionAcademica | `(anio, grupo, asignatura)` | Una materia de un grupo = un docente |
| PlanEstudios | `(anio, grado, asignatura)` | Una intensidad por materia/grado/año |
| Grupo | `(anio, codigo)` | Códigos únicos por año |
| Horario | `(anio, sede, version)` | Versiones claras |
| ClaseProgramada | `(horario, grupo*, dia, franja)` | Sin choque de grupo |
| ClaseProgramada | `(horario, docente*, dia, franja)` | Sin choque de docente |
| ClaseProgramada | `(horario, aula, dia, franja)` | Sin choque de aula |
| ParametroInstitucional | `(anio, clave)` | Un valor por parámetro/año |

---

## 7. Inventario de entidades (23)

| # | Entidad | Responsabilidad |
|---|---------|-----------------|
| 1 | Usuario | Acceso y personas |
| 2 | Docente | Acceso y personas |
| 3 | TituloProfesional | Acceso y personas |
| 4 | Idoneidad | Acceso y personas |
| 5 | AnioEscolar | Catálogo académico |
| 6 | Sede | Catálogo académico |
| 7 | Area | Catálogo académico |
| 8 | Asignatura | Catálogo académico |
| 9 | Grado | Catálogo académico |
| 10 | Grupo | Catálogo académico |
| 11 | PlanEstudios | Catálogo académico |
| 12 | Turno | Tiempo y espacio |
| 13 | FranjaHoraria | Tiempo y espacio |
| 14 | Aula | Tiempo y espacio |
| 15 | AsignacionAcademica | Asignación y horario |
| 16 | Horario | Asignación y horario |
| 17 | ClaseProgramada | Asignación y horario |
| 18 | ActividadInstitucional | Asignación y horario |
| 19 | AsignacionActividad | Asignación y horario |
| 20 | RestriccionDocente | Control y auditoría |
| 21 | ParametroInstitucional | Control y auditoría |
| 22 | Conflicto | Control y auditoría |
| 23 | RegistroAuditoria | Control y auditoría |
