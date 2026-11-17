import { api } from './client';
function textOrEmpty(value) {
    return value ?? '';
}
export function toProfessionalTitle(dto) {
    return {
        id: textOrEmpty(dto.id),
        level: textOrEmpty(dto.nivel),
        name: textOrEmpty(dto.nombreTitulo),
        institution: textOrEmpty(dto.institucion),
        year: dto.anioGraduacion == null ? '' : String(dto.anioGraduacion),
        supportFile: textOrEmpty(dto.archivoSoporte),
    };
}
export function listarTitulosPorDocente(docenteId, signal) {
    return api
        .get(`/api/v1/docentes/${docenteId}/titulos-profesionales`, { signal })
        .then((items) => (Array.isArray(items) ? items : []).map(toProfessionalTitle));
}
export function consultarTituloProfesional(id, signal) {
    return api
        .get(`/api/v1/titulos-profesionales/${id}`, {
        signal,
    })
        .then(toProfessionalTitle);
}
export function registrarTituloProfesional(payload) {
    return api
        .post('/api/v1/titulos-profesionales', payload)
        .then(toProfessionalTitle);
}
export function actualizarTituloProfesional(id, payload) {
    return api
        .put(`/api/v1/titulos-profesionales/${id}`, payload)
        .then(toProfessionalTitle);
}
