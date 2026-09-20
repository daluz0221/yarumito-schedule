create table catalogoacademico.grupo(
	id uuid primary key default gen_random_uuid(),
	codigo varchar(10) not null,
	grado_id uuid,
	anio_escolar_id uuid,
	sede_id uuid,
	director_grupo_id uuid,
	cantidad_estudiantes int,
	aula_fija uuid,
	activo bool not null,
	foreign key (grado_id) references catalogoacademico.grado (id),
	foreign key (anio_escolar_id) references catalogoacademico.anio_escolar(id),
	foreign key (sede_id) references catalogoacademico.anio_escolar (id),
	foreign key (director_grupo_id) references accesopersonas.docente (id),
	foreign key (aula_fija) references tiempoyespacio.aula(id)
);