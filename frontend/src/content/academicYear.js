export const academicYears = [
    {
        id: '2025',
        year: 2025,
        startDate: '20/01/2025',
        endDate: '28/11/2025',
        status: 'CERRADO',
        current: false,
    },
    {
        id: '2026',
        year: 2026,
        startDate: '19/01/2026',
        endDate: '27/11/2026',
        status: 'ACTIVO',
        current: true,
    },
    {
        id: '2027',
        year: 2027,
        startDate: '18/01/2027',
        endDate: '26/11/2027',
        status: 'PLANEACION',
        current: false,
    },
];
export const schoolGroups = [
    group('601', 'Sexto', 'BACHILLERATO', 32),
    group('602', 'Sexto', 'BACHILLERATO', 30),
    group('701', 'Séptimo', 'BACHILLERATO', 31),
    group('702', 'Séptimo', 'BACHILLERATO', 29),
    group('801', 'Octavo', 'BACHILLERATO', 28),
    group('802', 'Octavo', 'BACHILLERATO', 33),
    group('901', 'Noveno', 'BACHILLERATO', 27),
    group('1001', 'Décimo', 'MEDIA', 26),
    group('1002', 'Décimo', 'MEDIA', 24),
    group('1101', 'Undécimo', 'MEDIA', 22),
];
export const groupStatusFilterOptions = [
    { value: '', label: 'Todos' },
    { value: 'ACTIVO', label: 'Activo' },
    { value: 'INACTIVO', label: 'Inactivo' },
];
export const groupStatusOptions = groupStatusFilterOptions.filter((option) => option.value);
export const classroomOptions = [
    { value: '', label: 'Sin asignar' },
    { value: 'A-101', label: 'A-101' },
    { value: 'A-102', label: 'A-102' },
    { value: 'B-201', label: 'B-201' },
];
export function isGroupCodeTaken(groups, yearId, code, ignoreId) {
    const normalized = code.trim().toLowerCase();
    if (!normalized) {
        return false;
    }
    return groups.some((item) => item.yearId === yearId &&
        item.id !== ignoreId &&
        item.code.trim().toLowerCase() === normalized);
}
export function matchesAcademicYear(group, year) {
    return (group.yearId === year.id ||
        group.yearId === String(year.year) ||
        group.schoolYear === year.year);
}
export function academicYearOptions(years) {
    return years.map((year) => ({
        value: year.id,
        label: String(year.year),
    }));
}
export function labelYearStatus(status) {
    const labels = {
        CERRADO: 'Cerrado',
        ACTIVO: 'Activo',
        PLANEACION: 'Planeación',
    };
    return labels[status];
}
export const academicYearStatusOptions = ['PLANEACION', 'ACTIVO', 'CERRADO'].map((status) => ({
    value: status,
    label: labelYearStatus(status),
}));
export function suggestedAcademicYear(years) {
    const current = years.find((year) => year.current);
    const year = (current?.year ?? years.at(-1)?.year ?? 2026) + 1;
    return {
        year: String(year),
        startDate: `18/01/${year}`,
        endDate: `26/11/${year}`,
        status: 'PLANEACION',
    };
}
export function labelSchoolLevel(level) {
    const labels = {
        BACHILLERATO: 'Bachillerato',
        MEDIA: 'Media',
    };
    return labels[level];
}
export function labelGroupStatus(status) {
    const labels = {
        ACTIVO: 'Activo',
        INACTIVO: 'Inactivo',
    };
    return labels[status];
}
export function formatYearTimeline(years) {
    return [...years]
        .sort((left, right) => left.year - right.year)
        .map((year) => {
        const current = year.current ? ' (actual)' : '';
        return `${year.year} — ${labelYearStatus(year.status)}${current}`;
    })
        .join(' · ');
}
export function nextPlannedYear(years, selected) {
    return ([...years]
        .filter((year) => year.status === 'PLANEACION' && year.year > selected.year)
        .sort((left, right) => left.year - right.year)[0] ?? null);
}
export function filterSchoolGroups(groups, filters) {
    const query = filters.query.trim().toLowerCase();
    return groups.filter((item) => {
        if (filters.grade && item.grade !== filters.grade) {
            return false;
        }
        if (filters.status && item.status !== filters.status) {
            return false;
        }
        if (query && !item.code.toLowerCase().includes(query)) {
            return false;
        }
        return true;
    });
}
export function schoolGroupStats(groups) {
    const active = groups.filter((item) => item.status === 'ACTIVO');
    return [
        {
            id: 'active',
            value: String(active.length),
            label: 'Grupos activos',
        },
        {
            id: 'bachillerato',
            value: String(active.filter((item) => item.level === 'BACHILLERATO').length),
            label: 'Bachillerato',
        },
        {
            id: 'media',
            value: String(active.filter((item) => item.level === 'MEDIA').length),
            label: 'Media',
        },
    ];
}
function group(code, grade, level, students) {
    return {
        id: code,
        yearId: '2026',
        code,
        grade,
        level,
        weeklyHours: 30,
        students,
        director: null,
        status: 'ACTIVO',
    };
}
