create table catalogoacademico.asignatura(
	id uuid primary key default gen_random_uuid(),
	area_id uuid,
	nombre varchar(100) not null,
	codigo varchar(50) not null unique,
	abreviatura varchar(5) check (abreviatura in ('MAT','CAS','ING','CEN','SOC','ART','EFI','TEC','ETI','ERE','EMP','QUI','FIS','BIO','FILO','ESP')),
	color_ui varchar(6),
	exige_idoniedad_estricta bool not null,
	es_media_tecnica bool not null,
	requiere_docente_exclusivo bool not null,
	tipo_aula_requ varchar(20) check (tipo_aula_requ in ('AULA','LABORATORIO','SALA SISTEMAS','PATIO')),
	max_clases_consecutivas integer not null default 2,
	activa bool not null, 
	foreign key(area_id) references catalogoacademico.area (id)
);