create table asignacionyhorario.actividadInstitucional(
	id uuid primary key default gen_random_uuid(),
	anio_escolar_id uuid,
	nombre varchar(100) not null,
	tipo varchar(50) not null check(tipo in ('PROYECTO_PEDAGOGICO', 'ACTIVIDAD_INSTITUCIONAL', 'DIRECCION_DE_GRUPO', 'COMITE', 'OTRO')),
	descripcion text,
	horas_semanales int not null,
	foreign key (anio_escolar_id) references catalogoacademico.anio_escolar (id)
);