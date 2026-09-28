create table asignacionyhorario.claseProgramada(
	id uuid primary key default gen_random_uuid(),
	horario_id uuid unique,
	asignacion_academica_id uuid,
	dia VARCHAR(20) not null unique check (dia in ('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES')),
	franja_horaria_id uuid unique,
	aula_id uuid unique,
	es_hora_extra bool not null default false,
	bloqueada bool not null default false,
	foreign key (horario_id) references asignacionyhorario.horario (id),
	foreign key (asignacion_academica_id) references asignacionyhorario.asignacionacademica (id),
	foreign key (franja_horaria_id) references tiempoyespacio.franjahoraria (id),
	foreign key (aula_id) references tiempoyespacio.aula (id)
);