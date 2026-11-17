import { api } from './client';
import { horaCorta } from './turnos';
export function minutosDelDia(value) {
    const match = /^(\d{2}):(\d{2})/.exec(horaCorta(value ?? ''));
    if (!match) {
        return null;
    }
    return Number(match[1]) * 60 + Number(match[2]);
}
export function horaDesdeMinutos(total) {
    if (!Number.isInteger(total) || total < 0 || total >= 24 * 60) {
        return '';
    }
    const hours = Math.floor(total / 60);
    const minutes = total % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
}
export function consultarFranjaHoraria(id, signal) {
    return api.get(`/api/v1/franjas-horarias/${id}`, { signal });
}
export function registrarFranjaHoraria(payload) {
    return api.post('/api/v1/franjas-horarias', payload);
}
export function actualizarFranjaHoraria(id, payload) {
    return api.put(`/api/v1/franjas-horarias/${id}`, payload);
}
export function listarFranjasPorTurno(turnoId, signal) {
    const search = new URLSearchParams({ turnoId });
    return api.get(`/api/v1/franjas-horarias?${search.toString()}`, {
        signal,
    });
}
