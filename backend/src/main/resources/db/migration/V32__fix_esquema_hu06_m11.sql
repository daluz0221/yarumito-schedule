-- ============================================================================
-- V32: Correcciones de esquema para HU-06 (Asignación Académica)
--      y M-11 (Construcción de Horario).
--
-- Originalmente esta migración nació como V27, en paralelo con
-- V27__corregir_restricciones_catalogo_academico.sql del mainline. Al
-- fusionar ambas ramas quedaban dos migraciones con la misma versión y
-- Flyway fallaba al arrancar. Por eso se renumera a V32, después de las
-- migraciones V28 a V31 del mainline.
--
-- Este script NO modifica migraciones anteriores. Sólo agrega lo que el
-- mainline no cubre: valores por defecto y restricciones de unicidad
-- necesarias para HU-06 y M-11.
--
-- Es idempotente: si una base ya aplicó la antigua V27, puede volver a
-- ejecutar este script sin errores gracias a "drop constraint if exists".
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1. HU-06: fecha_asignacion con valor por defecto.
--    V16 la definió NOT NULL sin DEFAULT y la entidad JPA no la mapea, por lo
--    que cualquier INSERT realizado por Hibernate fallaba.
-- ----------------------------------------------------------------------------
alter table asignacionyhorario.asignacionacademica
    alter column fecha_asignacion set default now();


-- ----------------------------------------------------------------------------
-- 2. HU-06: impedir asignaciones duplicadas para el mismo año escolar,
--    grupo y asignatura.
-- ----------------------------------------------------------------------------
alter table asignacionyhorario.asignacionacademica
    drop constraint if exists uk_asignacion_academica_anio_grupo_asignatura;

alter table asignacionyhorario.asignacionacademica
    add constraint uk_asignacion_academica_anio_grupo_asignatura
    unique (anio_escolar_id, grupo_id, asignatura_id);


-- ----------------------------------------------------------------------------
-- 3. Franjas horarias: permitir el número 0 para franjas de descanso,
--    tal como lo documenta la entidad FranjaHoraria.
--
--    La corrección de la FK grupo.sede_id y de los CHECK de tipo de aula
--    ya la realiza V27__corregir_restricciones_catalogo_academico.sql.
-- ----------------------------------------------------------------------------
alter table tiempoyespacio.franjahoraria
    drop constraint if exists franjahoraria_numero_check;

alter table tiempoyespacio.franjahoraria
    add constraint franjahoraria_numero_check
    check (numero between 0 and 6);


-- ----------------------------------------------------------------------------
-- 4. M-11: impedir programaciones duplicadas.
--    - Una misma aula no puede tener dos clases en el mismo día/franja/horario.
--    - Una misma asignación académica no puede tener dos clases en el mismo
--      día/franja/horario.
--
--    Los choques de docente y grupo dependen de asignacionacademica y se
--    validarán en la capa de servicio.
-- ----------------------------------------------------------------------------
alter table asignacionyhorario.claseprogramada
    drop constraint if exists uk_clase_programada_aula_franja;

alter table asignacionyhorario.claseprogramada
    add constraint uk_clase_programada_aula_franja
    unique (horario_id, dia, franja_horaria_id, aula_id);


alter table asignacionyhorario.claseprogramada
    drop constraint if exists uk_clase_programada_asignacion_franja;

alter table asignacionyhorario.claseprogramada
    add constraint uk_clase_programada_asignacion_franja
    unique (horario_id, dia, franja_horaria_id, asignacion_academica_id);
