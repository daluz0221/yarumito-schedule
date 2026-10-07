create table catalogoacademico.planEstudios(
	id uuid primary key default gen_random_uuid(),
	anio_escolar_id uuid,
	grado_id uuid,
	asignatura_id uuid,
	horas_semanales int not null,
	turno_id uuid,
	observacion text,
	foreign key(anio_escolar_id) references catalogoacademico.anio_escolar(id),
	foreign key(grado_id) references catalogoacademico.grado (id),
	foreign key (asignatura_id) references catalogoacademico.asignatura (id),
	foreign key (turno_id) references tiempoyespacio.turno (id)
)