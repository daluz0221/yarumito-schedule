-- V29: Compatibilidad de abreviaturas del catálogo institucional.
-- Conserva las 16 abreviaturas anteriores y admite las 24
-- definidas en CatalogoAsignaturaController.java.

ALTER TABLE catalogoacademico.asignatura
DROP CONSTRAINT asignatura_abreviatura_check;

ALTER TABLE catalogoacademico.asignatura
ADD CONSTRAINT asignatura_abreviatura_check
CHECK (abreviatura IN (
    -- Abreviaturas históricas
    'MAT', 'CAS', 'ING', 'CEN',
    'SOC', 'ART', 'EFI', 'TEC',
    'ETI', 'ERE', 'EMP', 'QUI',
    'FIS', 'BIO', 'FILO', 'ESP',

    -- Catálogo institucional actual
    'BIOL', 'QUIM', 'FISI', 'AMBI',
    'SOCI', 'POLI', 'CIVI',
    'CAST', 'INGL', 'LECT',
    'MATE', 'GEOM', 'ESTAD',
    'INFO', 'EMPRE',
    'ETIC', 'RELI', 'ARTE',
    'EDFI', 'AGRO', 'VEGE',
    'TRAN', 'TECA'
));
