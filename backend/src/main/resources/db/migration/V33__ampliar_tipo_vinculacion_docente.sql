-- ============================================================================
-- V33: ampliar tipo_vinculacion del docente.
--
-- V4 declaró la columna como varchar(10), pero el enum TipoVinculacion
-- incluye "PROVISIONAL" (11 caracteres) y el propio CHECK de V4 lo admite.
-- Cualquier docente con esa vinculación fallaba al insertarse.
--
-- Es idempotente: volver a aplicar el cambio de tipo no altera el resultado.
-- ============================================================================
alter table accesopersonas.docente
    alter column tipo_vinculacion type varchar(20);
