create table asignacionyhorario.asignacionAcademica(
	id uuid primary key default gen_random_uuid(),
	anio_escolar_id uuid unique,
	docente_id uuid,
	grupo_id uuid unique,
	asignatura_id uuid unique,
	horas_semanales int not null,
	horas_extras int not null default 0,
	estado VARCHAR(20) not null check (estado in ('BORRADOR', 'CONFIRMADA')),
	observacion text,
	asignada_por uuid,
	fecha_asignacion timestamp not null,
	foreign key (anio_escolar_id) references catalogoacademico.anio_escolar (id),
	foreign key (docente_id) references accesopersonas.docente (id),
	foreign key (grupo_id) references catalogoacademico.grupo (id),
	foreign key (asignatura_id) references catalogoacademico.asignatura (id),
	foreign key (asignada_por) references accesopersonas.usuario (id)
);