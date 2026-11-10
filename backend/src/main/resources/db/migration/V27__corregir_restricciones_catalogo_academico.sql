-- Alinear los tipos de aula de Asignatura con TipoAulaRequerida.
ALTER TABLE catalogoacademico.asignatura
DROP CONSTRAINT asignatura_tipo_aula_requ_check;

ALTER TABLE catalogoacademico.asignatura
ADD CONSTRAINT asignatura_tipo_aula_requ_check
CHECK (tipo_aula_requ IN (
    'AULA',
    'LABORATORIO',
    'SALA_SISTEMAS',
    'CANCHA'
));

-- Corregir el valor SALA_SITEMAS en Aula.
ALTER TABLE tiempoyespacio.aula
DROP CONSTRAINT aula_tipo_check;

ALTER TABLE tiempoyespacio.aula
ADD CONSTRAINT aula_tipo_check
CHECK (tipo IN (
    'AULA',
    'LABORATORIO',
    'SALA_SISTEMAS',
    'CANCHA'
));

-- Corregir la relación entre Grupo y Sede.
ALTER TABLE catalogoacademico.grupo
DROP CONSTRAINT grupo_sede_id_fkey;

ALTER TABLE catalogoacademico.grupo
ADD CONSTRAINT grupo_sede_id_fkey
FOREIGN KEY (sede_id)
REFERENCES catalogoacademico.sede(id);
