import { api } from './client';
const PAGE_SIZE = 100;
function listarPagina(page, signal) {
    const search = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
    });
    return api.get(`/api/v1/grados?${search.toString()}`, { signal });
}
export function consultarGrado(id, signal) {
    return api.get(`/api/v1/grados/${id}`, { signal });
}
export function registrarGrado(payload) {
    return api.post('/api/v1/grados', payload);
}
export function actualizarGrado(id, payload) {
    return api.put(`/api/v1/grados/${id}`, payload);
}
export async function listarCatalogoGrados(signal) {
    const first = await listarPagina(0, signal);
    const grados = [...(first.content ?? [])];
    const totalPages = first.totalPages ?? 1;
    for (let page = 1; page < totalPages; page += 1) {
        const next = await listarPagina(page, signal);
        grados.push(...(next.content ?? []));
    }
    return ordenarGrados(grados);
}
export function etiquetaGrado(nombre) {
    const limpio = nombre.trim().toLocaleLowerCase('es');
    if (!limpio) {
        return 'Grado';
    }
    return limpio.charAt(0).toLocaleUpperCase('es') + limpio.slice(1);
}
export function ordenarGrados(grados) {
    return [...grados].sort((left, right) => left.nivel - right.nivel ||
        etiquetaGrado(left.nombre).localeCompare(etiquetaGrado(right.nombre), 'es'));
}
export function planDesdeGrado(grado) {
    return {
        typeLabel: grado.esMedia ? 'Media Técnica' : 'Bachillerato',
        weeklyHours: grado.horasSemanalesEsperadas,
    };
}
export function gradoPorId(grados, id) {
    return grados?.find((grado) => grado.id === id) ?? null;
}
export function gradoPorEtiqueta(grados, etiqueta) {
    return grados?.find((grado) => etiquetaGrado(grado.nombre) === etiqueta) ?? null;
}
function opcionesEstado(grados, error) {
    if (error) {
        return [{ value: '', label: 'Grados no disponibles' }];
    }
    if (grados === null) {
        return [{ value: '', label: 'Cargando grados…' }];
    }
    if (grados.length === 0) {
        return [{ value: '', label: 'Sin grados registrados' }];
    }
    return null;
}
export function opcionesGradoRequerido(grados, error) {
    return (opcionesEstado(grados, error) ?? [
        { value: '', label: 'Seleccione un grado' },
        ...ordenarGrados(grados ?? []).map((grado) => ({
            value: grado.id,
            label: etiquetaGrado(grado.nombre),
        })),
    ]);
}
export function opcionesFiltroGrados(grados, error) {
    if (error) {
        return [{ value: '', label: 'Grados no disponibles' }];
    }
    if (grados === null) {
        return [{ value: '', label: 'Cargando grados…' }];
    }
    return [
        { value: '', label: 'Todos' },
        ...ordenarGrados(grados).map((grado) => ({
            value: etiquetaGrado(grado.nombre),
            label: etiquetaGrado(grado.nombre),
        })),
    ];
}
export function opcionesSelectorPlan(grados, error) {
    return (opcionesEstado(grados, error) ??
        ordenarGrados(grados ?? []).map((grado) => ({
            value: grado.id,
            label: etiquetaGrado(grado.nombre),
        })));
}
