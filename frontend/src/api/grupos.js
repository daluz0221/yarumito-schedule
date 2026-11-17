import { etiquetaGrado, gradoPorId, planDesdeGrado } from './grados';
import { ApiError, api } from './client';
const PAGE_SIZE = 100;
function listarPagina(page, signal) {
    const search = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
    });
    return api.get(`/api/v1/grupos?${search.toString()}`, { signal });
}
function esMedia(dto, grado) {
    if (grado) {
        return grado.esMedia ? 'MEDIA' : 'BACHILLERATO';
    }
    return dto.gradoNivel >= 10 ? 'MEDIA' : 'BACHILLERATO';
}
export function aSchoolGroup(dto, grados) {
    if (!dto?.id || !dto.anioEscolarId || !dto.codigo?.trim()) {
        return null;
    }
    const grado = grados ? gradoPorId(grados, dto.gradoId) : null;
    const plan = grado ? planDesdeGrado(grado) : null;
    return {
        id: dto.id,
        yearId: dto.anioEscolarId,
        schoolYear: Number.isFinite(dto.anioEscolar) ? dto.anioEscolar : undefined,
        code: dto.codigo.trim(),
        grade: etiquetaGrado(dto.gradoNombre || grado?.nombre || ''),
        level: esMedia(dto, grado),
        weeklyHours: plan?.weeklyHours ?? null,
        students: dto.cantidadEstudiantes ?? 0,
        director: dto.directorGrupoNombre?.trim() || null,
        status: dto.activo ? 'ACTIVO' : 'INACTIVO',
        sedeId: dto.sedeId || undefined,
        sedeNombre: dto.sedeNombre?.trim() || undefined,
    };
}
export function gruposVisibles(grupos, grados) {
    return (grupos ?? []).flatMap((dto) => {
        const group = aSchoolGroup(dto, grados);
        return group ? [group] : [];
    });
}
export function consultarGrupo(id, signal) {
    return api.get(`/api/v1/grupos/${id}`, { signal });
}
function codigoDesdeRespuesta(payload) {
    if (typeof payload === 'string' && payload.trim()) {
        return payload.trim();
    }
    if (typeof payload === 'number' && Number.isFinite(payload)) {
        return String(payload);
    }
    throw new ApiError('No se pudo leer el código sugerido', 200);
}
export function sugerirCodigoGrupo(gradoId, anioEscolarId, signal) {
    const search = new URLSearchParams({ gradoId, anioEscolarId });
    return api
        .get(`/api/v1/grupos/siguiente-codigo?${search.toString()}`, { signal })
        .then(codigoDesdeRespuesta);
}
export function registrarGrupo(payload) {
    return api.post('/api/v1/grupos', payload);
}
export function actualizarGrupo(id, payload) {
    return api.put(`/api/v1/grupos/${id}`, payload);
}
export async function listarCatalogoGrupos(signal) {
    const first = await listarPagina(0, signal);
    const grupos = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(page, signal);
        grupos.push(...(next.content ?? []));
    }
    return grupos.sort((left, right) => (left.gradoNivel ?? 0) - (right.gradoNivel ?? 0) ||
        (left.codigo ?? '').localeCompare(right.codigo ?? '', 'es'));
}
