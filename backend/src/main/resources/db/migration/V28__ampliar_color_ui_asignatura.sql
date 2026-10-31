-- Corrige la longitud del campo color_ui en Asignaturas.
-- Permite almacenar colores hexadecimales con el prefijo #.
ALTER TABLE catalogoacademico.asignatura
    ALTER COLUMN color_ui TYPE VARCHAR(10);
