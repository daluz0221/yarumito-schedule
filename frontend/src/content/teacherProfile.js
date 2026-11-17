import { PENDING_BACKEND_LABEL } from './teachers';
export const emptyCrearIdoneidadForm = {
    areaId: '',
    asignaturaId: '',
    tipo: 'PRINCIPAL',
    tituloSoporteId: '',
    justificacion: '',
    vigenteDesde: '',
    vigenteHasta: '',
};
export const emptyCrearTituloForm = {
    level: '',
    name: '',
    institution: '',
    year: '',
    supportFile: '',
};
const NIVEL_TITULO_LABELS = {
    NORMALISTA: 'Normalista',
    LICENCIATURA: 'Licenciatura',
    PROFESIONAL: 'Profesional',
    ESPECIALIZACION: 'Especialización',
    MAESTRIA: 'Maestría',
    DOCTORADO: 'Doctorado',
};
export const titleLevelOptions = [
    { value: '', label: 'Seleccione un nivel' },
    ...Object.entries(NIVEL_TITULO_LABELS).map(([value, label]) => ({
        value,
        label,
    })),
];
export const suitabilityTypeOptions = [
    { value: 'PRINCIPAL', label: 'Principal' },
    { value: 'AUTORIZADA', label: 'Autorizada' },
    { value: 'EXCEPCIONAL', label: 'Excepcional' },
];
export function labelTipoIdoneidad(tipo) {
    const labels = {
        PRINCIPAL: 'Principal',
        AUTORIZADA: 'Autorizada',
        EXCEPCIONAL: 'Excepcional',
    };
    return labels[tipo];
}
function fechaLocalISO(date = new Date()) {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
}
// Vigente desde el día de inicio hasta el día de cierre, ambos inclusive.
// Sin fecha de fin, sigue vigente.
export function isIdoneidadVigente(item, hoy = fechaLocalISO()) {
    const desde = item.vigenteDesde.slice(0, 10);
    const hasta = item.vigenteHasta.slice(0, 10);
    if (desde && hoy < desde) {
        return false;
    }
    if (!hasta) {
        return true;
    }
    return hoy <= hasta;
}
function resumenIdoneidades(idoneidades) {
    const porArea = idoneidades.filter((item) => !item.asignaturaId.trim()).length;
    const porAsignatura = idoneidades.filter((item) => item.asignaturaId.trim()).length;
    if (porArea > 0 && porAsignatura > 0) {
        return {
            estado: 'listo',
            tipo: 'mixto',
            areas: porArea,
            asignaturas: porAsignatura,
        };
    }
    if (porAsignatura > 0) {
        return { estado: 'listo', tipo: 'asignaturas', cantidad: porAsignatura };
    }
    if (porArea > 0) {
        return { estado: 'listo', tipo: 'areas', cantidad: porArea };
    }
    return { estado: 'listo', tipo: 'pendiente' };
}
export function resumenAcademicoDocente(titulos, idoneidades) {
    return {
        titulos: titulos === null
            ? { estado: 'error' }
            : { estado: 'listo', cantidad: titulos.length },
        idoneidad: idoneidades === null ? { estado: 'error' } : resumenIdoneidades(idoneidades),
    };
}
export function labelConteoTitulos(cantidad) {
    return cantidad === 1 ? '1 registrado' : `${cantidad} registrados`;
}
function labelCantidad(cantidad, singular, plural) {
    return `${cantidad} ${cantidad === 1 ? singular : plural}`;
}
export function labelResumenIdoneidad(resumen) {
    if (resumen.tipo === 'pendiente') {
        return 'Pendiente';
    }
    if (resumen.tipo === 'asignaturas') {
        return labelCantidad(resumen.cantidad, 'asignatura', 'asignaturas');
    }
    if (resumen.tipo === 'mixto') {
        return `${labelCantidad(resumen.areas, 'área', 'áreas')}, ${labelCantidad(resumen.asignaturas, 'asignatura', 'asignaturas')}`;
    }
    return labelCantidad(resumen.cantidad, 'área', 'áreas');
}
export function formatVigencia(item) {
    if (item.vigenteHasta) {
        return `${item.vigenteDesde} – ${item.vigenteHasta}`;
    }
    return item.vigenteDesde;
}
function optionalText(value) {
    const trimmed = value.trim();
    return trimmed === '' ? undefined : trimmed;
}
export function toCrearIdoneidadRequest(docenteId, form) {
    if (!form.areaId) {
        throw new Error('El área es obligatoria');
    }
    if (!form.vigenteDesde) {
        throw new Error('La fecha de inicio de vigencia es obligatoria');
    }
    return {
        docenteId,
        areaId: form.areaId,
        asignaturaId: optionalText(form.asignaturaId),
        tipo: form.tipo,
        tituloSoporteId: optionalText(form.tituloSoporteId),
        justificacion: optionalText(form.justificacion),
        vigenteDesde: form.vigenteDesde,
        vigenteHasta: optionalText(form.vigenteHasta),
    };
}
export function toActualizarIdoneidadRequest(form) {
    if (!form.areaId) {
        throw new Error('El área es obligatoria');
    }
    if (!form.vigenteDesde) {
        throw new Error('La fecha de inicio de vigencia es obligatoria');
    }
    return {
        areaId: form.areaId,
        asignaturaId: optionalText(form.asignaturaId),
        tipo: form.tipo,
        tituloSoporteId: optionalText(form.tituloSoporteId),
        justificacion: optionalText(form.justificacion),
        vigenteDesde: form.vigenteDesde,
        vigenteHasta: optionalText(form.vigenteHasta),
    };
}
export function toFinalizarVigenciaRequest(vigenteHasta) {
    return { vigenteHasta };
}
function toTituloProfesionalPayload(form) {
    const nombreTitulo = form.name.trim();
    if (!form.level || !(form.level in NIVEL_TITULO_LABELS)) {
        throw new Error('El nivel del título es obligatorio');
    }
    if (!nombreTitulo) {
        throw new Error('El nombre del título es obligatorio');
    }
    return {
        nivel: form.level,
        nombreTitulo,
        institucion: optionalText(form.institution),
        anioGraduacion: parseAnioGraduacion(form.year),
        archivoSoporte: optionalText(form.supportFile),
    };
}
export function toCrearTituloProfesionalRequest(docenteId, form) {
    return {
        docenteId,
        ...toTituloProfesionalPayload(form),
    };
}
export function toActualizarTituloProfesionalRequest(form) {
    return toTituloProfesionalPayload(form);
}
function parseAnioGraduacion(value) {
    const trimmed = value.trim();
    if (!trimmed) {
        return undefined;
    }
    if (!/^\d{4}$/.test(trimmed)) {
        throw new Error('El año de graduación debe tener 4 dígitos');
    }
    const year = Number(trimmed);
    const maxYear = new Date().getFullYear() + 1;
    if (year < 1900 || year > maxYear) {
        throw new Error(`El año de graduación debe estar entre 1900 y ${maxYear}`);
    }
    return year;
}
export function idoneidadToForm(item) {
    return {
        areaId: item.areaId,
        asignaturaId: item.asignaturaId,
        tipo: item.tipo,
        tituloSoporteId: item.tituloSoporteId,
        justificacion: item.justificacion,
        vigenteDesde: item.vigenteDesde.slice(0, 10),
        vigenteHasta: item.vigenteHasta.slice(0, 10),
    };
}
export function labelNivelTitulo(nivel) {
    return NIVEL_TITULO_LABELS[nivel] ?? (nivel || 'Sin nivel');
}
export function formatTituloSoporte(title) {
    const year = title.year ? ` (${title.year})` : '';
    return `${labelNivelTitulo(title.level)} · ${title.name}${year}`;
}
export function resolveTituloSoporte(tituloId, titles) {
    if (!tituloId) {
        return 'Sin título';
    }
    const title = titles.find((item) => item.id === tituloId);
    return title ? formatTituloSoporte(title) : 'Título no disponible';
}
export function resolveAreaName(areaId, areas) {
    return areas.find((area) => area.id === areaId)?.nombre ?? PENDING_BACKEND_LABEL;
}
export function formatDocumentNumber(value) {
    return value.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}
export { PENDING_BACKEND_LABEL };
