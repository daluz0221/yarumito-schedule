create table controlyauditoria.conflicto(
	id uuid primary key default gen_random_uuid(),
	horario_id uuid,
	tipo varchar(100) not null check (
		tipo in ('CHOQUE_DOCENTE','CHOQUE_GRUPO','CHOQUE_AULA','EXCEDE_HORAS_CONTRACTUALES',
				'EXCEDE_HORAS_EXTRAS','CLASES_CONSECUTIVAS_EXCEDIDAS','DOCENTE_SIN_IDONEIDAD',
				'DOCENTE_NO_DISPONIBLE','TURNO_INCORRECTO','PLAN_ESTUDIOS_INCORRECTO','HORAS_ASIGNACION_NO_CUADRAN',
				'AULA_TIPO_INCORRECTO','CAPACIDAD_AULA','CARGA_AREA_NOMBRAMIENTO')
		),
	severidad varchar(50) not null check (severidad in ('ERROR','ADVERTENCIA')),
	mensaje text not null,
	resuelto bool not null default false,
	fecha_detencion timestamp not null,
	asignacion_id uuid,
	docente_id uuid,
	grupo_id uuid,
	foreign key (horario_id) references asignacionyhorario.horario (id),
	foreign key (asignacion_id) references asignacionyhorario.asignacionactividad  (id),
	foreign key (docente_id) references accesopersonas.docente (id),
	foreign key (grupo_id) references catalogoacademico.grupo (id)
)