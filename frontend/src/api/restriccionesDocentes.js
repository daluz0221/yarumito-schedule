import { api } from './client';
const DIA_VISTA = {
    LUNES: 'Lunes',
    MARTES: 'Martes',
    MIERCOLES: 'Miércoles',
    JUEVES: 'Jueves',
    VIERNES: 'Viernes',
};
const DIA_API = {
    Lunes: 'LUNES',
    Martes: 'MARTES',
    Miércoles: 'MIERCOLES',
    Jueves: 'JUEVES',
    Viernes: 'VIERNES',
};
export function diaApi(day) {
    return DIA_API[day];
}
export function tipoApi(status) {
    return status === 'PREFERENCIA' ? 'PREFERENTE' : 'NO_DISPONIBLE';
}
function documento(value) {
    return typeof value === 'string' && value.trim() ? value.trim() : null;
}
export function aRestriccionVista(dto) {
    const day = DIA_VISTA[dto?.dia];
    const status = dto?.tipo === 'PREFERENTE'
        ? 'PREFERENCIA'
        : dto?.tipo === 'NO_DISPONIBLE'
            ? 'NO_DISPONIBLE'
            : null;
    if (!dto?.id || !day || !status || !dto.franjaHorariaId) {
        return null;
    }
    return {
        id: dto.id,
        docenteId: dto.docenteId,
        anioEscolarId: dto.anioEscolarId,
        day,
        bandId: dto.franjaHorariaId,
        status,
        motivo: dto.motivo?.trim() ?? '',
        documentoSoporte: documento(dto.documentoSoporte),
        aprobadaPorId: dto.aprobadaPorId ?? null,
    };
}
function listar(path, signal) {
    return api.get(path, { signal }).then((items) => (Array.isArray(items) ? items : []).flatMap((item) => {
        const vista = aRestriccionVista(item);
        return vista ? [vista] : [];
    }));
}
export function consultarRestriccion(id, signal) {
    return api.get(`/api/v1/restricciones-docentes/${id}`, { signal });
}
export function listarRestriccionesPorDocenteYAnio(docenteId, anioEscolarId, signal) {
    return listar(`/api/v1/restricciones-docentes/docente/${docenteId}/anio/${anioEscolarId}`, signal);
}
export function listarRestriccionesPorAnio(anioEscolarId, signal) {
    return listar(`/api/v1/restricciones-docentes/anio/${anioEscolarId}`, signal);
}
export function registrarRestriccion(payload) {
    return api.post('/api/v1/restricciones-docentes', payload);
}
export function actualizarRestriccion(id, payload) {
    return api.put(`/api/v1/restricciones-docentes/${id}`, payload);
}
