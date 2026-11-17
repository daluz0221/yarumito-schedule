import { api } from './client';
export function registrarAsignacion(payload) {
    return api.post('/api/asignaciones', payload);
}
export function listarBorradores(signal) {
    return api.get('/api/asignaciones/borradores', { signal });
}
export function consultarAsignacion(id, signal) {
    return api.get(`/api/asignaciones/${id}`, { signal });
}
export function listarAsignacionesPorDocente(docenteId, signal) {
    return api.get(`/api/asignaciones/docente/${docenteId}`, {
        signal,
    });
}
export function listarAsignacionesPorGrupo(grupoId, signal) {
    return api.get(`/api/asignaciones/grupo/${grupoId}`, { signal });
}
export function confirmarAsignacion(id) {
    return api.patch(`/api/asignaciones/${id}/confirmar`);
}
