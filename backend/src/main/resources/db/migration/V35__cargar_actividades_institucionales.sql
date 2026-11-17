-- Catálogo mínimo para asignar actividades desde la pantalla existente.
-- Los identificadores coinciden con el listado del frontend.
insert into asignacionyhorario.actividadinstitucional (
    id,
    anio_escolar_id,
    nombre,
    tipo,
    descripcion,
    horas_semanales
)
select
    '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e501'::uuid,
    (
        select id
        from catalogoacademico.anio_escolar
        order by es_actual desc, anio desc
        limit 1
    ),
    'Proyecto de acompañamiento institucional',
    'PROYECTO_PEDAGOGICO',
    'Actividad institucional para completar la carga docente.',
    2
where not exists (
    select 1
    from asignacionyhorario.actividadinstitucional
    where id = '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e501'::uuid
);

insert into asignacionyhorario.actividadinstitucional (
    id,
    anio_escolar_id,
    nombre,
    tipo,
    descripcion,
    horas_semanales
)
select
    '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e502'::uuid,
    (
        select id
        from catalogoacademico.anio_escolar
        order by es_actual desc, anio desc
        limit 1
    ),
    'Proyecto de convivencia',
    'PROYECTO_PEDAGOGICO',
    'Actividad institucional para completar la carga docente.',
    2
where not exists (
    select 1
    from asignacionyhorario.actividadinstitucional
    where id = '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e502'::uuid
);

insert into asignacionyhorario.actividadinstitucional (
    id,
    anio_escolar_id,
    nombre,
    tipo,
    descripcion,
    horas_semanales
)
select
    '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e503'::uuid,
    (
        select id
        from catalogoacademico.anio_escolar
        order by es_actual desc, anio desc
        limit 1
    ),
    'Proyecto de emprendimiento',
    'PROYECTO_PEDAGOGICO',
    'Actividad institucional para completar la carga docente.',
    2
where not exists (
    select 1
    from asignacionyhorario.actividadinstitucional
    where id = '6f1c2a10-4b7e-4c3a-9d21-a1b2c3d4e503'::uuid
);
