create table accesopersonas.titulo_profesional(
	id uuid primary key default gen_random_uuid(),
	docente_id uuid,
	nivel varchar(20) not null check (nivel in ('NORMALISTA', 'LICENCIATURA', 'PROFESIONAL', 'ESPECIALIZACION', 'MAESTRIA', 'DOCTORADO')),
	nombre_titulo varchar(100) not null,
	institucion varchar(100),
	anio_graduacion integer,
	archivo_soporte bytea,
	foreign key (docente_id) references accesopersonas.docente (id)
);