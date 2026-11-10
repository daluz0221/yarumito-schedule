-- PROVISIONAL tiene 11 caracteres y no cabe en varchar(10).
ALTER TABLE accesopersonas.docente
    ALTER COLUMN tipo_vinculacion TYPE varchar(20);
