-- ============================================================================
-- V27: Correcciones de esquema para HU-06 (Asignación Académica)
--      y M-11 (Construcción de Horario).
--
-- Este script NO modifica migraciones anteriores. Sólo agrega valores por
-- defecto, corrige llaves foráneas/CHECK y agrega restricciones de unicidad
-- necesarias para las funcionalidades nuevas.
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
    add constraint uk_asignacion_academica_anio_grupo_asignatura
    unique (anio_escolar_id, grupo_id, asignatura_id);


-- ----------------------------------------------------------------------------
-- 3. Catálogo: corregir la FK de grupo.sede_id.
--    V12 la declaró apuntando a catalogoacademico.anio_escolar(id) en lugar de
--    catalogoacademico.sede(id).
--
--    Se busca dinámicamente el nombre de la FK existente sobre la columna
--    sede_id para poder eliminarla sin depender del nombre autogenerado.
-- ----------------------------------------------------------------------------
do $$
declare
    v_constraint text;
begin
    select con.conname
      into v_constraint
      from pg_constraint con
      join pg_class rel on rel.oid = con.conrelid
      join pg_namespace nsp on nsp.oid = rel.relnamespace
     where nsp.nspname = 'catalogoacademico'
       and rel.relname = 'grupo'
       and con.contype = 'f'
       and con.conkey = array[
           (select attnum
              from pg_attribute
             where attrelid = rel.oid
               and attname = 'sede_id')
       ];

    if v_constraint is not null then
        execute format(
            'alter table catalogoacademico.grupo drop constraint %I',
            v_constraint);
    end if;
end $$;

alter table catalogoacademico.grupo
    add constraint grupo_sede_id_fkey
    foreign key (sede_id) references catalogoacademico.sede (id);


-- ----------------------------------------------------------------------------
-- 4. Catálogo: alinear los valores de tipo de aula con el enum
--    TipoAulaRequerida (AULA, LABORATORIO, SALA_SISTEMAS, CANCHA).
--
--    V11 usaba el typo 'SALA_SITEMAS' y V6 usaba 'SALA SISTEMAS' y 'PATIO'.
--    Primero se normalizan los datos existentes y luego se reemplazan los CHECK.
-- ----------------------------------------------------------------------------
update tiempoyespacio.aula
   set tipo = 'SALA_SISTEMAS'
 where tipo in ('SALA_SITEMAS', 'SALA SISTEMAS');

alter table tiempoyespacio.aula
    drop constraint if exists aula_tipo_check;

alter table tiempoyespacio.aula
    add constraint aula_tipo_check
    check (tipo in ('AULA', 'LABORATORIO', 'SALA_SISTEMAS', 'CANCHA'));


update catalogoacademico.asignatura
   set tipo_aula_requ = 'SALA_SISTEMAS'
 where tipo_aula_requ = 'SALA SISTEMAS';

update catalogoacademico.asignatura
   set tipo_aula_requ = 'CANCHA'
 where tipo_aula_requ = 'PATIO';

alter table catalogoacademico.asignatura
    drop constraint if exists asignatura_tipo_aula_requ_check;

alter table catalogoacademico.asignatura
    add constraint asignatura_tipo_aula_requ_check
    check (tipo_aula_requ in ('AULA', 'LABORATORIO', 'SALA_SISTEMAS', 'CANCHA'));


-- ----------------------------------------------------------------------------
-- 5. Franjas horarias: permitir el número 0 para franjas de descanso,
--    tal como lo documenta la entidad FranjaHoraria.
-- ----------------------------------------------------------------------------
alter table tiempoyespacio.franjahoraria
    drop constraint if exists franjahoraria_numero_check;

alter table tiempoyespacio.franjahoraria
    add constraint franjahoraria_numero_check
    check (numero between 0 and 6);


-- ----------------------------------------------------------------------------
-- 6. M-11: impedir programaciones duplicadas.
--    - Una misma aula no puede tener dos clases en el mismo día/franja/horario.
--    - Una misma asignación académica no puede tener dos clases en el mismo
--      día/franja/horario.
--
--    Los choques de docente y grupo dependen de asignacionacademica y se
--    validarán en la capa de servicio.
-- ----------------------------------------------------------------------------
alter table asignacionyhorario.claseprogramada
    add constraint uk_clase_programada_aula_franja
    unique (horario_id, dia, franja_horaria_id, aula_id);

alter table asignacionyhorario.claseprogramada
    add constraint uk_clase_programada_asignacion_franja
    unique (horario_id, dia, franja_horaria_id, asignacion_academica_id);
