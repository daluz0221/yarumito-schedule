create table asignacionyhorario.horario(
	id uuid primary key default gen_random_uuid(),
	anio_escolar_id uuid unique,
	sede_id uuid unique,
	version int unique not null,
	nombre varchar(100) not null,
	estado varchar(20) not null check (estado in ('BORRADOR','PUBLICADO','ARCHIVADO')),
	fecha_publicacion timestamp ,
	creado_por uuid,
	notas text,
	foreign key (anio_escolar_id) references catalogoacademico.anio_escolar (id),
	foreign key (sede_id) references catalogoacademico.sede (id),
	foreign key (creado_por) references accesopersonas.usuario (id)
);