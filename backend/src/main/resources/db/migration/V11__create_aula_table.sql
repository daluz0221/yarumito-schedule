create table tiempoyespacio.aula(
	id uuid primary key default gen_random_uuid(),
	sede_id uuid,
	nombre varchar(100) not null,
	tipo VARCHAR(50) not null check(tipo in ('AULA','LABORATORIO','SALA_SITEMAS','CANCHA')),
	capacidad int,
	activa bool,
	foreign key (sede_id) references catalogoacademico.sede (id)
)