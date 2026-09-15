create table accesopersonas.idoniedad(
	id uuid primary key default gen_random_uuid(),
	docente_id uuid,
	area_id uuid,
	asignatura_id uuid,
	titulo_id uuid,
	tipo varchar(20) not null check (tipo in ('PRINCIPAL', 'AUTORIZADA', 'EXCEPCIONAL')),
	justificacion text,
	vigente_desde date not null,
	vigente_hasta date,
	aprobada_por uuid,
	foreign key (docente_id) references accesopersonas.docente (id),
	foreign key (area_id) references catalogoacademico.area (id),
	foreign key (asignatura_id) references catalogoacademico.asignatura (id),
	foreign key (titulo_id) references accesopersonas.titulo_profesional (id),
	foreign key (aprobada_por) references accesopersonas.usuario (id)
);