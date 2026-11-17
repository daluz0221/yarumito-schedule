import { api } from './client';
const PAGE_SIZE = 100;
function listarPagina(page, signal) {
    const search = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
    });
    return api.get(`/api/v1/turnos?${search.toString()}`, { signal });
}
export function horaCorta(value) {
    const match = /^(\d{2}:\d{2})/.exec(value ?? '');
    return match?.[1] ?? value;
}
export function consultarTurno(id, signal) {
    return api.get(`/api/v1/turnos/${id}`, { signal });
}
export function registrarTurno(payload) {
    return api.post('/api/v1/turnos', payload);
}
export function actualizarTurno(id, payload) {
    return api.put(`/api/v1/turnos/${id}`, payload);
}
export async function listarCatalogoTurnos(signal) {
    const first = await listarPagina(0, signal);
    const turnos = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(page, signal);
        turnos.push(...(next.content ?? []));
    }
    return turnos.sort((left, right) => left.nombre.localeCompare(right.nombre, 'es'));
}
