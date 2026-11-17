export const availabilityDays = [
    'Lunes',
    'Martes',
    'Miércoles',
    'Jueves',
    'Viernes',
];
export const morningShiftLabel = 'Mañana';
// Jornada de la mañana según el diseño. El descanso no se edita.
export const morningBands = [
    { id: '0630', label: '6:30 – 7:25', kind: 'class' },
    { id: '0725', label: '7:25 – 8:20', kind: 'class' },
    { id: '0820', label: '8:20 – 9:15', kind: 'class' },
    { id: 'break', label: 'Descanso · 9:15 – 9:45 · No editable', kind: 'break' },
    { id: '0945', label: '9:45 – 10:40', kind: 'class' },
    { id: '1040', label: '10:40 – 11:35', kind: 'class' },
];
// El backend aún no expone restricciones. 2026 reproduce la grilla del diseño
// para Carlos Pérez (en el listado real el nombre llega completo). 2025, año
// cerrado, solo conserva el lunes 6:30 del detalle de solo lectura.
const designMarks = [
    {
        yearId: '2025',
        bandId: '0630',
        day: 'Lunes',
        status: 'NO_DISPONIBLE',
        motivo: 'Estudios de especialización',
        fileName: 'soporte_estudios.pdf',
        approvedBy: 'Rector / usuario autenticado',
    },
    {
        yearId: '2026',
        bandId: '0630',
        day: 'Lunes',
        status: 'NO_DISPONIBLE',
        motivo: 'Estudios de especialización',
        fileName: 'soporte_estudios.pdf',
        approvedBy: 'Rector / usuario autenticado',
    },
    {
        yearId: '2026',
        bandId: '0725',
        day: 'Martes',
        status: 'NO_DISPONIBLE',
        motivo: 'Compromiso institucional',
        fileName: null,
        approvedBy: null,
    },
    {
        yearId: '2026',
        bandId: '1040',
        day: 'Miércoles',
        status: 'PREFERENCIA',
        motivo: '',
        fileName: null,
        approvedBy: null,
    },
];
const statusLabels = {
    DISPONIBLE: 'Disponible',
    NO_DISPONIBLE: 'No disponible',
    PREFERENCIA: 'Preferencia',
};
export function labelAvailability(status) {
    return statusLabels[status];
}
export function restrictionCountLabel(count) {
    return count === 1 ? '1 restricción registrada' : `${count} restricciones registradas`;
}
function normalizeName(value) {
    return value
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}
function isDesignTeacher(name) {
    const normalized = normalizeName(name);
    return normalized.includes('carlos') && normalized.includes('perez');
}
function matchesContext(mark, yearId, teacherName) {
    return mark.yearId === yearId && isDesignTeacher(teacherName);
}
export function restrictionDayOptions() {
    return [
        { value: '', label: 'Seleccione un día' },
        ...availabilityDays.map((day) => ({ value: day, label: day })),
    ];
}
export function restrictionBandOptions() {
    return [
        { value: '', label: 'Seleccione una franja' },
        ...morningBands
            .filter((band) => band.kind === 'class')
            .map((band) => ({ value: band.id, label: band.label })),
    ];
}
export const restrictionTypeOptions = [
    { value: '', label: 'Seleccione un tipo' },
    { value: 'NO_DISPONIBLE', label: 'No disponible' },
    { value: 'PREFERENCIA', label: 'Preferencia' },
];
export const restrictionTypeHint = 'Tipos permitidos: No disponible · Preferencia';
export const restrictionEditTypeHint = 'Opciones: No disponible · Preferencia';
export const restrictionApprovalHint = 'Aprobación: el Rector autenticado se registra cuando corresponde.';
export const restrictionEditApprovalHint = 'Aprobación: gestionada por el Rector autenticado cuando corresponda.';
export function timeBandLabel(bandId) {
    return morningBands.find((band) => band.id === bandId)?.label ?? '';
}
export function restrictionEffect(status) {
    if (status === 'NO_DISPONIBLE') {
        return 'Esta franja no podrá utilizarse al programar clases para el docente.';
    }
    return 'Esta condición será tenida en cuenta durante la planificación y generará una advertencia.';
}
export function findTeacherRestriction(yearId, teacherName, bandId, day) {
    return designMarks.find((mark) => matchesContext(mark, yearId, teacherName) && mark.bandId === bandId && mark.day === day);
}
export function availabilityStatus(yearId, teacherName, bandId, day) {
    return (designMarks.find((mark) => matchesContext(mark, yearId, teacherName) && mark.bandId === bandId && mark.day === day)?.status ?? 'DISPONIBLE');
}
export function countRestrictions(yearId, teacherName) {
    return designMarks.filter((mark) => matchesContext(mark, yearId, teacherName)).length;
}
export function preferredRestrictionTeacher(names) {
    return names.find((name) => isDesignTeacher(name));
}
