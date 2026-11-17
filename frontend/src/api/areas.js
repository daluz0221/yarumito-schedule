import { api } from './client';
const PAGE_SIZE = 100;
function buildAreasPath(query) {
    const search = new URLSearchParams();
    const texto = query.texto?.trim();
    if (texto) {
        search.set('texto', texto);
    }
    if (typeof query.activa === 'boolean') {
        search.set('activa', String(query.activa));
    }
    search.set('page', String(query.page));
    search.set('size', String(query.size));
    return `/api/v1/areas?${search.toString()}`;
}
function listarPagina(query, page, signal) {
    return api.get(buildAreasPath({ ...query, page, size: PAGE_SIZE }), { signal });
}
async function listarTodas(query, signal) {
    const first = await listarPagina(query, 0, signal);
    const areas = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(query, page, signal);
        areas.push(...(next.content ?? []));
    }
    return areas;
}
export function listarAreas(signal, query = {}) {
    return listarTodas(query, signal);
}
export function listarCatalogoAreas(signal) {
    return listarTodas({}, signal);
}
export function registrarArea(payload) {
    return api.post('/api/v1/areas', payload);
}
export function consultarArea(id, signal) {
    return api.get(`/api/v1/areas/${id}`, { signal });
}
export function actualizarArea(id, payload) {
    return api.put(`/api/v1/areas/${id}`, payload);
}
export function cambiarEstadoArea(id, activa) {
    const search = new URLSearchParams({ activa: String(activa) });
    return api.patch(`/api/v1/areas/${id}/estado?${search.toString()}`);
}
export function toAreaSelectOptions(areas) {
    return [
        { value: '', label: 'Seleccione un área' },
        ...areas.map((area) => ({
            value: area.id,
            label: area.nombre,
        })),
    ];
}
