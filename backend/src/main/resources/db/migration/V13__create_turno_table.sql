create table tiempoyespacio.turno(
	id uuid primary key default gen_random_uuid(),
	nombre varchar(100) not null check (nombre in ('Mañana','Contrajornada')),
	hora_inicio time not null,
	hora_fin time not null,
	duracion_clase_minutos int not null,
	clases_por_dia int not null,
	clases_antes_de_descanso int not null,
	duracion_descanso_minutos int not null
)
