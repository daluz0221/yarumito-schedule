create table catalogoacademico.anio_escolar(
	id uuid primary key default gen_random_uuid(),
	anio integer not null,
	fecha_inicio date not null,
	fecha_fin date not null,
	estado varchar(20) not null check (estado in ('PLANEACION', 'ACTIVO', 'CERRADO')),
	es_actual bool not null
);