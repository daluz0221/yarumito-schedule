-- V30: Compatibilidad del escalafón docente con el catálogo Java.
-- Se conservan temporalmente las categorías históricas.

ALTER TABLE accesopersonas.docente
DROP CONSTRAINT docente_escalafon_check;

ALTER TABLE accesopersonas.docente
ADD CONSTRAINT docente_escalafon_check
CHECK (escalafon IN (
    -- Valores históricos
    'G1A', 'G1B', 'G1C', 'G1D',
    'G2A', 'G2B', 'G2C', 'G2D', 'G3',

    -- Decreto 2277: grados 1 a 14
    '1', '2', '3', '4', '5', '6', '7',
    '8', '9', '10', '11', '12', '13', '14',

    -- Decreto 1278: grados y niveles
    '1A', '1B', '1C', '1D',
    '2A', '2B', '2C', '2D',
    '3A', '3B', '3C', '3D',

    -- SEIP
    'I', 'II', 'III', 'IV', 'V', 'VI'
));
