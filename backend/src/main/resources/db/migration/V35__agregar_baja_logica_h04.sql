
-- ============================================================
-- V35 - H-04: Baja lógica de registros administrativos
-- Proyecto: Gestión de Horarios I.E. Rural Yarumito
-- ============================================================

-- 1. PLAN DE ESTUDIOS
-- Permite retirar registros conservando su historial.

ALTER TABLE catalogoacademico.planestudios
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;


-- 2. TÍTULOS PROFESIONALES
-- Conserva los títulos retirados y sus relaciones
-- con las idoneidades históricas de los docentes.

ALTER TABLE accesopersonas.titulo_profesional
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;


-- 3. RESTRICCIONES DOCENTES
-- Permite retirar restricciones sin eliminar
-- sus datos ni documentos de soporte.

ALTER TABLE controlyauditoria.restriccion_docente
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
