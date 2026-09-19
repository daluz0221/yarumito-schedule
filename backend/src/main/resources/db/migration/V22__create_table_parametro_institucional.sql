create table controlyauditoria.parametroInstitucional(
	id uuid primary key default gen_random_uuid(),
	anio_escolar_id uuid unique,
	clave varchar(50) not null,
	valor varchar(10) not null,
	tipo_dato varchar(50) not null,
	descripcion text,
	foreign key (anio_escolar_id) references catalogoacademico.anio_escolar (id)
)
