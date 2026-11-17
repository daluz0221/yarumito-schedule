import { api } from './client';
import { horaCorta } from './turnos';
const DIAS = {
    LUNES: 'Lunes',
    MARTES: 'Martes',
    MIERCOLES: 'Miércoles',
    JUEVES: 'Jueves',
    VIERNES: 'Viernes',
};
function horaVisible(value) {
    return horaCorta(value ?? '').replace(/^0(?=\d:)/, '');
}
const DIA_API = {
    Lunes: 'LUNES',
    Martes: 'MARTES',
    Miércoles: 'MIERCOLES',
    Jueves: 'JUEVES',
    Viernes: 'VIERNES',
};
export function diaSemanaApi(day) {
    return DIA_API[day];
}
export function consultarHorarioGrupo(grupoId, signal) {
    return api.get(`/api/horarios/grupos/${grupoId}`, { signal });
}
export function crearClaseProgramada(payload) {
    return api.post('/api/clases-programadas', payload);
}
export function actualizarClaseProgramada(id, payload) {
    return api.put(`/api/clases-programadas/${id}`, payload);
}
export function eliminarClaseProgramada(id) {
    return api.delete(`/api/clases-programadas/${id}`);
}
export function etiquetaEstadoHorario(estado) {
    if (estado === 'PUBLICADO' || estado === 'ARCHIVADO' || estado === 'BORRADOR') {
        return estado;
    }
    return 'BORRADOR';
}
export function bandasDelTablero(franjas) {
    return [...(franjas ?? [])]
        .sort((left, right) => left.orden - right.orden || left.numero - right.numero)
        .map((franja) => {
        const horario = `${horaVisible(franja.horaInicio)} – ${horaVisible(franja.horaFin)}`;
        return {
            id: franja.id,
            kind: franja.esDescanso ? 'break' : 'class',
            label: franja.esDescanso ? `DESCANSO · ${horario} · No programable` : horario,
        };
    });
}
export function clasesDelTablero(tablero) {
    return (tablero.clasesProgramadas ?? []).flatMap((clase) => {
        const day = DIAS[clase.dia];
        if (!day || !clase.id || !clase.franjaHorariaId) {
            return [];
        }
        const warnings = (clase.warnings ?? []).map((item) => item.trim()).filter(Boolean);
        const extra = clase.esHoraExtra ? 'Hora extra.' : '';
        const reason = [...warnings, extra].filter(Boolean).join(' ');
        return [
            {
                id: clase.id,
                yearId: tablero.anioEscolarId,
                groupId: tablero.grupoId,
                day,
                bandId: clase.franjaHorariaId,
                subjectKey: clase.asignacionAcademicaId,
                subject: clase.asignaturaNombre?.trim() || 'Asignatura',
                teacherName: clase.docenteNombre?.trim() || 'Docente',
                classroom: clase.aulaNombre?.trim() || 'Sin aula',
                aulaId: clase.aulaId ?? '',
                tone: warnings.length > 0 ? 'warning' : 'ok',
                reason,
                locked: Boolean(clase.bloqueada),
            },
        ];
    });
}
export function pendientesDelTablero(tablero) {
    return (tablero.asignacionesPendientes ?? []).map((item) => ({
        key: item.asignacionAcademicaId,
        subject: item.asignaturaNombre?.trim() || 'Asignatura',
        teacherName: item.docenteNombre?.trim() || 'Docente',
        pendingHours: item.horasPendientes,
        note: null,
    }));
}
export function metricasDelTablero(resumen) {
    return {
        programmed: resumen?.asignacionesProgramadas ?? 0,
        assignedCount: resumen?.totalAsignaciones ?? 0,
        placedHours: resumen?.horasProgramadas ?? 0,
        assignedHours: resumen?.totalHoras ?? 0,
        pendingHours: resumen?.horasPendientes ?? 0,
        conflicts: resumen?.conflictos ?? 0,
        warnings: resumen?.advertencias ?? 0,
    };
}
