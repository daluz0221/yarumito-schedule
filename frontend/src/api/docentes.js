import { api } from './client';
import { teacherStatsFromCounts, } from '../content/teachers';
function textOrEmpty(value) {
    return value ?? '';
}
export function toTeacher(dto) {
    return {
        id: dto.id,
        nombres: textOrEmpty(dto.nombres),
        apellidos: textOrEmpty(dto.apellidos),
        tipoDocumento: textOrEmpty(dto.tipoDocumento),
        numeroDocumento: textOrEmpty(dto.numeroDocumento),
        telefono: textOrEmpty(dto.telefono),
        correoInstitucional: textOrEmpty(dto.correoInstitucional),
        tipoVinculacion: dto.tipoVinculacion,
        areaNombramientoId: textOrEmpty(dto.areaNombramientoId),
        numeroDecreto: textOrEmpty(dto.numeroDecreto),
        fechaDecreto: textOrEmpty(dto.fechaDecreto),
        escalafon: textOrEmpty(dto.escalafon),
        horasSemanalesContratadas: dto.horasSemanalesContratadas ?? 0,
        maxHorasExtra: dto.maxHorasExtra ?? 0,
        esExclusivoMediaTecnica: Boolean(dto.esExclusivoMediaTecnica),
        estado: dto.estado,
        fechaVinculacion: textOrEmpty(dto.fechaVinculacion),
    };
}
function buildQuery(params) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value === undefined || value === '') {
            return;
        }
        search.set(key, String(value));
    });
    const query = search.toString();
    return query ? `/api/v1/docentes?${query}` : '/api/v1/docentes';
}
export function registrarDocente(payload) {
    return api.post('/api/v1/docentes', payload);
}
export function consultarDocente(id, signal) {
    return api
        .get(`/api/v1/docentes/${id}`, { signal })
        .then(toTeacher);
}
export function actualizarDocente(id, payload) {
    return api.put(`/api/v1/docentes/${id}`, payload);
}
export function cambiarEstadoDocente(id, nuevoEstado) {
    return api
        .patch(`/api/v1/docentes/${id}/estado`, { nuevoEstado })
        .then(toTeacher);
}
export function listarDocentes(query, signal) {
    return api
        .get(buildQuery({
        texto: query.texto?.trim(),
        estado: query.estado,
        tipoVinculacion: query.tipoVinculacion,
        areaId: query.areaId,
        page: query.page,
        size: query.size,
    }), { signal })
        .then((page) => ({
        content: (page.content ?? []).map(toTeacher),
        total: page.totalElements ?? 0,
    }));
}
async function countDocentes(estado, signal) {
    const page = await api.get(buildQuery({
        estado,
        page: 0,
        size: 1,
    }), { signal });
    return page.totalElements ?? 0;
}
export async function consultarResumenDocentes(signal) {
    const [total, active, license, retired] = await Promise.all([
        countDocentes(undefined, signal),
        countDocentes('ACTIVO', signal),
        countDocentes('LICENCIA', signal),
        countDocentes('RETIRADO', signal),
    ]);
    return teacherStatsFromCounts({ total, active, license, retired });
}
