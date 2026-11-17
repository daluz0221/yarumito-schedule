import { api } from './client';
const TIPOS = {
    DOCENTE_OCUPADO: 'teacher-busy',
    AULA_OCUPADA: 'classroom-busy',
    CAPACIDAD_AULA: 'capacity',
};
export function listarConflictosPorHorario(horarioId, signal) {
    return api.get(`/api/v1/conflictos/horario/${horarioId}`, { signal });
}
export function listarConflictosPendientes(horarioId, signal) {
    return api.get(`/api/v1/conflictos/horario/${horarioId}/pendientes`, {
        signal,
    });
}
export function listarConflictosPorSeveridad(horarioId, severidad, signal) {
    const search = new URLSearchParams({ severidad });
    return api.get(`/api/v1/conflictos/horario/${horarioId}/severidad?${search.toString()}`, { signal });
}
export function marcarConflictoResuelto(conflictoId) {
    return api.patch(`/api/v1/conflictos/${conflictoId}/resuelto`);
}
function formatoFechaDeteccion(value) {
    if (Array.isArray(value) && value.length >= 5) {
        const [year, month, day, hour, minute] = value.map((part) => Number(part));
        const pad = (part) => String(part).padStart(2, '0');
        if ([year, month, day, hour, minute].every((part) => Number.isFinite(part))) {
            return `${year}-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)}`;
        }
    }
    if (typeof value === 'string') {
        const match = /^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2})/.exec(value);
        return match ? `${match[1]} ${match[2]}` : value;
    }
    return '—';
}
export function aConflictoRevisado(dto, yearId) {
    const kind = TIPOS[dto?.tipo];
    if (!dto?.id || !kind) {
        return null;
    }
    const severity = dto.severidad === 'ADVERTENCIA' ? 'ADVERTENCIA' : 'ERROR';
    return {
        id: dto.id,
        yearId,
        severity,
        kind,
        message: dto.mensaje?.trim() || 'Conflicto detectado',
        day: null,
        bandId: null,
        slotLabel: '—',
        affected: '—',
        subject: '',
        teacherName: '',
        classroom: '',
        groupCode: '',
        groupIds: [],
        teacherNames: [],
        classrooms: [],
        status: dto.resuelto ? 'RESUELTO' : 'PENDIENTE',
        detectedAt: formatoFechaDeteccion(dto.fechaDeteccion),
    };
}
