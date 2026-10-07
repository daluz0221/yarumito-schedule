create table catalogoacademico.grado(
	id uuid primary key default gen_random_uuid(),
	nivel int not null check (nivel between 6 and 11),
	es_media bool not null,
	prioridad_asignacion int not null,
	horas_semanales_esperadas int not null
);