import { api } from './client';
const PAGE_SIZE = 100;
function buildAsignaturasPath(query) {
    const search = new URLSearchParams();
    const texto = query.texto?.trim();
    if (texto) {
        search.set('texto', texto);
    }
    if (typeof query.activa === 'boolean') {
        search.set('activa', String(query.activa));
    }
    if (query.areaId) {
        search.set('areaId', query.areaId);
    }
    search.set('page', String(query.page));
    search.set('size', String(query.size));
    return `/api/v1/asignaturas?${search.toString()}`;
}
function listarPagina(query, page, signal) {
    return api.get(buildAsignaturasPath({ ...query, page, size: PAGE_SIZE }), { signal });
}
async function listarTodas(query, signal) {
    const first = await listarPagina(query, 0, signal);
    const asignaturas = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(query, page, signal);
        asignaturas.push(...(next.content ?? []));
    }
    return asignaturas;
}
export function listarAsignaturas(areaId, signal, query = {}) {
    return listarTodas({ ...query, areaId }, signal);
}
export function consultarAsignatura(id, signal) {
    return api.get(`/api/v1/asignaturas/${id}`, { signal });
}
export function registrarAsignatura(payload) {
    return api.post('/api/v1/asignaturas', payload);
}
export function actualizarAsignatura(id, payload) {
    return api.put(`/api/v1/asignaturas/${id}`, payload);
}
export function cambiarEstadoAsignatura(id, activa) {
    const search = new URLSearchParams({ activa: String(activa) });
    return api.patch(`/api/v1/asignaturas/${id}/estado?${search.toString()}`);
}
