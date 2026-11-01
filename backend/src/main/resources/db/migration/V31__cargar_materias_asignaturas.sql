-- V31: Carga inicial del catálogo institucional.
-- 8 materias (áreas) y 24 asignaturas.
-- Conserva registros existentes y evita duplicados por código.

INSERT INTO catalogoacademico.area
    (codigo, nombre, obligatoria, solo_media, activa)
VALUES
    ('NAT', 'Ciencias Naturales y Educación Ambiental', TRUE, FALSE, TRUE),
    ('SOC', 'Ciencias Sociales, Historia, Geografía y Democracia', TRUE, FALSE, TRUE),
    ('HUM', 'Humanidades', TRUE, FALSE, TRUE),
    ('MAT', 'Matemáticas', TRUE, FALSE, TRUE),
    ('TEC', 'Tecnología, Informática y Emprendimiento', TRUE, FALSE, TRUE),
    ('ETI', 'Educación Ética y Religiosa', TRUE, FALSE, TRUE),
    ('WEL', 'Áreas del Bienestar', TRUE, FALSE, TRUE),
    ('AGR', 'Media Técnica Agropecuaria', FALSE, TRUE, TRUE)
ON CONFLICT (codigo) DO NOTHING;

WITH catalogo (
    codigo, nombre, abreviatura, codigo_area, es_media_tecnica
) AS (
    VALUES
    ('NAT-BIO', 'Biología / Ciencias Naturales', 'BIOL', 'NAT', FALSE),
    ('NAT-QUI', 'Química', 'QUIM', 'NAT', FALSE),
    ('NAT-FIS', 'Física', 'FISI', 'NAT', FALSE),
    ('NAT-AMB', 'Educación Ambiental y Ecológica', 'AMBI', 'NAT', FALSE),

    ('SOC-SOC', 'Ciencias Sociales', 'SOCI', 'SOC', FALSE),
    ('SOC-POL', 'Ciencias Económicas y Políticas', 'POLI', 'SOC', FALSE),
    ('SOC-CIV', 'Competencias Ciudadanas / Cátedra de la Paz', 'CIVI', 'SOC', FALSE),

    ('HUM-CAS', 'Lengua Castellana y Literatura', 'CAST', 'HUM', FALSE),
    ('HUM-ING', 'Inglés', 'INGL', 'HUM', FALSE),
    ('HUM-LEC', 'Comprensión Lectora', 'LECT', 'HUM', FALSE),

    ('MAT-MAT', 'Matemáticas Generales', 'MATE', 'MAT', FALSE),
    ('MAT-GEO', 'Geometría', 'GEOM', 'MAT', FALSE),
    ('MAT-EST', 'Estadística', 'ESTAD', 'MAT', FALSE),

    ('TEC-INF', 'Informática', 'INFO', 'TEC', FALSE),
    ('TEC-EMP', 'Emprendimiento', 'EMPRE', 'TEC', FALSE),

    ('ETI-VAL', 'Ética y Valores', 'ETIC', 'ETI', FALSE),
    ('ETI-REL', 'Educación Religiosa', 'RELI', 'ETI', FALSE),
    ('ETI-PHI', 'Filosofía', 'FILO', 'ETI', FALSE),

    ('WEL-ART', 'Educación Artística y Cultural', 'ARTE', 'WEL', FALSE),
    ('WEL-EFI', 'Educación Física, Recreación y Deportes', 'EDFI', 'WEL', FALSE),

    ('AGR-AGR', 'Especialidad Agropecuaria General', 'AGRO', 'AGR', TRUE),
    ('AGR-VEG', 'Producción Vegetal y Cultivos', 'VEGE', 'AGR', TRUE),
    ('AGR-TRA', 'Transformación y Aprovechamiento', 'TRAN', 'AGR', TRUE),
    ('AGR-TEC', 'Tecnología e Innovación Agrícola', 'TECA', 'AGR', TRUE)
)
INSERT INTO catalogoacademico.asignatura (
    codigo,
    nombre,
    abreviatura,
    area_id,
    exige_idoniedad_estricta,
    es_media_tecnica,
    requiere_docente_exclusivo,
    max_clases_consecutivas,
    activa
)
SELECT
    c.codigo,
    c.nombre,
    c.abreviatura,
    a.id,
    FALSE,
    c.es_media_tecnica,
    FALSE,
    2,
    TRUE
FROM catalogo c
JOIN catalogoacademico.area a
    ON a.codigo = c.codigo_area
ON CONFLICT (codigo) DO NOTHING;
