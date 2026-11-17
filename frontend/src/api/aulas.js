import { api } from './client';
export const tiposAula = [
    { value: 'AULA', label: 'Aula' },
    { value: 'LABORATORIO', label: 'Laboratorio' },
    { value: 'SALA_SISTEMAS', label: 'Sala de sistemas' },
    { value: 'CANCHA', label: 'Cancha' },
];
export function etiquetaTipoAula(tipo) {
    return tiposAula.find((item) => item.value === tipo)?.label ?? tipo;
}
export function listarAulasPorSede(sedeId, signal) {
    const search = new URLSearchParams({ sedeId });
    return api.get(`/api/v1/aulas?${search.toString()}`, { signal });
}
export function consultarAula(id, signal) {
    return api.get(`/api/v1/aulas/${id}`, { signal });
}
export function registrarAula(payload) {
    return api.post('/api/v1/aulas', payload);
}
export function actualizarAula(id, payload) {
    return api.put(`/api/v1/aulas/${id}`, payload);
}
export function registrarNuevaVersionAula(id, payload) {
    return api.post(`/api/v1/aulas/${id}/nueva-version`, payload);
}
export function eliminarAula(id) {
    return api.delete(`/api/v1/aulas/${id}`);
}
