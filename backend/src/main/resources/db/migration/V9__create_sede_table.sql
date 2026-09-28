create table catalogoacademico.sede(
	id uuid primary key default gen_random_uuid(),
	nombre varchar(100) not null,
	codigo varchar(20) not null,
	direccion varchar(100),
	es_principal bool not null
);