import { api } from './client';
function textOrEmpty(value) {
    return value ?? '';
}
export function toIdoneidad(dto) {
    return {
        id: dto.id,
        docenteId: dto.docenteId,
        areaId: textOrEmpty(dto.areaId),
        asignaturaId: textOrEmpty(dto.asignaturaId),
        tipo: dto.tipo,
        tituloSoporteId: textOrEmpty(dto.tituloSoporteId),
        justificacion: textOrEmpty(dto.justificacion),
        vigenteDesde: textOrEmpty(dto.vigenteDesde),
        vigenteHasta: textOrEmpty(dto.vigenteHasta),
        aprobadaPorId: textOrEmpty(dto.aprobadaPorId),
    };
}
export function listarIdoneidadesPorDocente(docenteId, signal) {
    return api
        .get(`/api/v1/idoneidades/docente/${docenteId}`, {
        signal,
    })
        .then((items) => (items ?? []).map(toIdoneidad));
}
export function consultarIdoneidad(id, signal) {
    return api
        .get(`/api/v1/idoneidades/${id}`, { signal })
        .then(toIdoneidad);
}
export function registrarIdoneidad(payload) {
    return api
        .post('/api/v1/idoneidades', payload)
        .then(toIdoneidad);
}
export function actualizarIdoneidad(id, payload) {
    return api
        .put(`/api/v1/idoneidades/${id}`, payload)
        .then(toIdoneidad);
}
export function finalizarVigenciaIdoneidad(id, payload) {
    return api
        .patch(`/api/v1/idoneidades/${id}/vigencia`, payload)
        .then(toIdoneidad);
}
