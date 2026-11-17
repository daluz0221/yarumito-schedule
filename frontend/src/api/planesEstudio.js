import { api } from './client';
const PAGE_SIZE = 100;
function listarPagina(page, signal) {
    const search = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
    });
    return api.get(`/api/v1/planes-estudio?${search.toString()}`, {
        signal,
    });
}
export function consultarPlanEstudios(id, signal) {
    return api.get(`/api/v1/planes-estudio/${id}`, { signal });
}
export function registrarPlanEstudios(payload) {
    return api.post('/api/v1/planes-estudio', payload);
}
export function actualizarPlanEstudios(id, payload) {
    return api.put(`/api/v1/planes-estudio/${id}`, payload);
}
export async function listarPlanesEstudio(signal) {
    const first = await listarPagina(0, signal);
    const planes = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(page, signal);
        planes.push(...(next.content ?? []));
    }
    return planes;
}
