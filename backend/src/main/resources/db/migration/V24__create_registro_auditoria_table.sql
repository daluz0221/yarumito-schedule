create table controlyauditoria.registroauditoria(
	id uuid primary key default gen_random_uuid(),
	usuario_id uuid,
	accion varchar(50) not null,
	entidad varchar(50) not null,
	id_identidad uuid not null,
	datos_antes json,
	datos_despues json,
	fecha timestamp not null,
	ip varchar(20),
	foreign key (usuario_id) references accesopersonas.usuario (id)
)