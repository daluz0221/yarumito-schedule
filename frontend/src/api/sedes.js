import { api } from './client';
const PAGE_SIZE = 100;
function listarPagina(page, signal) {
    const search = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
    });
    return api.get(`/api/v1/sedes?${search.toString()}`, { signal });
}
export async function listarCatalogoSedes(signal) {
    const first = await listarPagina(0, signal);
    const sedes = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(page, signal);
        sedes.push(...(next.content ?? []));
    }
    return sedes;
}
export function consultarSede(id, signal) {
    return api.get(`/api/v1/sedes/${id}`, { signal });
}
export function registrarSede(payload) {
    return api.post('/api/v1/sedes', payload);
}
export function actualizarSede(id, payload) {
    return api.put(`/api/v1/sedes/${id}`, payload);
}
export function sedeVisible(sedes) {
    return sedes.find((sede) => sede.esPrincipal) ?? sedes[0] ?? null;
}
export function opcionesCatalogoSedes(sedes, error) {
    if (error) {
        return [{ value: '', label: 'Sede no disponible' }];
    }
    if (sedes === null) {
        return [{ value: '', label: 'Cargando sedes…' }];
    }
    if (sedes.length === 0) {
        return [{ value: '', label: 'Sin sedes registradas' }];
    }
    return sedes.map((sede) => ({
        value: sede.id,
        label: sede.nombre,
    }));
}
export function etiquetaCatalogoSedes(sedes, error) {
    if (error) {
        return 'Sede no disponible';
    }
    if (sedes === null) {
        return 'Cargando sedes…';
    }
    return sedeVisible(sedes)?.nombre ?? 'Sin sedes registradas';
}
