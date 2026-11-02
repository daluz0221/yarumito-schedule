create table asignacionyhorario.asignacionActividad(
	id uuid primary key default gen_random_uuid(),
	docente_id uuid,
	actividad_id uuid,
	horas_semanales int not null,
	justificacion text,
	foreign key (docente_id) references accesopersonas.docente (id),
	foreign key (actividad_id) references asignacionyhorario.actividadinstitucional (id)
);