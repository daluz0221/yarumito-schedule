create table tiempoyespacio.franjaHoraria(
	id uuid primary key default gen_random_uuid(),
	turno_id uuid,
	numero int not null check (numero between 1 and 6),
	hora_inicio time not null,
	hora_fin time not null,
	es_descanso bool not null,
	horas_academicas_equivalentes decimal not null,
	orden int not null,
	foreign key (turno_id) references tiempoyespacio.turno (id)
);