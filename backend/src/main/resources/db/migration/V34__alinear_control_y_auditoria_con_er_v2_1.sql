-- Alinea el módulo Control y Auditoría con el Modelo ER V2.1.
-- Corrige estructuras históricas sin modificar las migraciones
-- V21, V22, V23 y V24 ya existentes.

-- ============================================================
-- CONFLICTO
-- Migración histórica: V23
-- ============================================================

ALTER TABLE controlyauditoria.conflicto
    RENAME COLUMN fecha_detencion TO fecha_deteccion;

ALTER TABLE controlyauditoria.conflicto
DROP COLUMN IF EXISTS asignacion_id,
    DROP COLUMN IF EXISTS docente_id,
    DROP COLUMN IF EXISTS grupo_id;

ALTER TABLE controlyauditoria.conflicto
DROP CONSTRAINT IF EXISTS conflicto_tipo_check;

ALTER TABLE controlyauditoria.conflicto
    ADD CONSTRAINT conflicto_tipo_check
        CHECK (
            tipo IN (
                     'DOCENTE_OCUPADO',
                     'AULA_OCUPADA',
                     'CAPACIDAD_AULA'
                )
            );

ALTER TABLE controlyauditoria.conflicto
DROP CONSTRAINT IF EXISTS conflicto_severidad_check;

ALTER TABLE controlyauditoria.conflicto
    ADD CONSTRAINT conflicto_severidad_check
        CHECK (
            severidad IN (
                          'ERROR',
                          'ADVERTENCIA'
                )
            );


-- ============================================================
-- PARAMETRO INSTITUCIONAL
-- Migración histórica: V22
-- ============================================================

ALTER TABLE controlyauditoria.parametroInstitucional
    RENAME TO parametro_institucional;

ALTER TABLE controlyauditoria.parametro_institucional
DROP CONSTRAINT IF EXISTS parametroinstitucional_anio_escolar_id_fkey;

ALTER TABLE controlyauditoria.parametro_institucional
    ADD CONSTRAINT parametro_institucional_anio_escolar_fk
        FOREIGN KEY (anio_escolar_id)
            REFERENCES catalogoacademico.anio_escolar(id);

ALTER TABLE controlyauditoria.parametro_institucional
    ADD CONSTRAINT parametro_institucional_anio_clave_uk
        UNIQUE (anio_escolar_id, clave);


-- ============================================================
-- REGISTRO AUDITORIA
-- Migración histórica: V24
-- ============================================================

ALTER TABLE controlyauditoria.registroauditoria
    RENAME TO registro_auditoria;

ALTER TABLE controlyauditoria.registro_auditoria
    RENAME COLUMN id_identidad TO id_entidad;

ALTER TABLE controlyauditoria.registro_auditoria
ALTER COLUMN datos_antes
        TYPE jsonb
        USING datos_antes::jsonb;

ALTER TABLE controlyauditoria.registro_auditoria
ALTER COLUMN datos_despues
        TYPE jsonb
        USING datos_despues::jsonb;


-- ============================================================
-- RESTRICCION DOCENTE
-- Migración histórica: V21
-- ============================================================

ALTER TABLE controlyauditoria.restricciondocente
    RENAME TO restriccion_docente;