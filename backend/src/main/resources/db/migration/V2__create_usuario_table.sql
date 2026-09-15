create table AccesoPersonas.usuario(
	id uuid primary key default gen_random_uuid(),
	correo varchar(100) unique not null,
	contrasena_hash varchar(64) not null,
	rol varchar(10) not null check (rol in ('DOCENTE','RECTOR')) default 'RECTOR',
	activo boolean not null default true,
	ultimo_acceso timestamp 
);

