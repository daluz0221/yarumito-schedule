create table catalogoacademico.area(
	id uuid primary key default gen_random_uuid(),
	nombre varchar(100) not null,
	codigo varchar(50) unique not null,
	obligatoria bool not null default true,
	solo_media bool not null default true,
	activa bool not null
);
