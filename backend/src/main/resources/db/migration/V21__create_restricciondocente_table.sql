create table controlyauditoria.restricciondocente(
	id uuid primary key default gen_random_uuid(),
	docente_id uuid,
	anio_escolar_id uuid,
	dia varchar(20) not null check (dia in ('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES')),
	franja_horaria_id uuid,
	tipo varchar(20) not null check (tipo in ('NO_DISPONIBLE', 'PREFERENTE')),
	motivo text not null,
	documento_soporte bytea,
	aprobada_por uuid,
	foreign key (docente_id) references accesopersonas.docente (id),
	foreign key (anio_escolar_id) references catalogoacademico.anio_escolar (id),
	foreign key (franja_horaria_id) references tiempoyespacio.franjahoraria (id),
	foreign key (aprobada_por) references accesopersonas.usuario (id)
)