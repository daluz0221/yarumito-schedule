-- Guarda el contexto del conflicto para poder mostrarlo en el detalle.
-- Los registros anteriores quedan con estas columnas en null.

ALTER TABLE controlyauditoria.conflicto
    ADD COLUMN dia varchar(20),
    ADD COLUMN franja varchar(40),
    ADD COLUMN elemento_afectado varchar(160),
    ADD COLUMN asignatura varchar(160),
    ADD COLUMN docente varchar(160),
    ADD COLUMN aula varchar(160),
    ADD COLUMN grupo varchar(40);
