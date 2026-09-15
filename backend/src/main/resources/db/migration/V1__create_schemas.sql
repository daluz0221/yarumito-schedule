do $$
declare
	esquema text;
	lista_squemas text[] := array['accesopersonas','catalogoacademico','tiempoyespacio','asignacionyhorario','controlyauditoria'];
begin
	foreach esquema in array lista_squemas
	loop
		execute format('create schema if not exists %I;', esquema);
	end loop;
end $$;




