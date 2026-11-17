import { academicYears, labelYearStatus, } from './academicYear';
export const studyPlanGrades = [
    { id: '6', label: '6°', level: 'Bachillerato', weeklyHours: 30 },
    { id: '7', label: '7°', level: 'Bachillerato', weeklyHours: 30 },
    { id: '8', label: '8°', level: 'Bachillerato', weeklyHours: 30 },
    { id: '9', label: '9°', level: 'Bachillerato', weeklyHours: 30 },
    { id: '10', label: '10°', level: 'Media Técnica', weeklyHours: 37 },
    { id: '11', label: '11°', level: 'Media Técnica', weeklyHours: 37 },
];
// Filas de referencia para pantallas que aún no consultan el plan.
// La pantalla de plan de estudios lee GET /api/v1/planes-estudio.
export const studyPlanEntries = [
    {
        id: '2026-6-mat',
        yearId: '2026',
        gradeId: '6',
        area: 'Matemáticas',
        subject: 'Matemáticas',
        weeklyHours: 5,
        shift: 'Mañana',
        note: null,
    },
    {
        id: '2026-6-bio',
        yearId: '2026',
        gradeId: '6',
        area: 'Ciencias Naturales',
        subject: 'Biología',
        weeklyHours: 4,
        shift: 'Mañana',
        note: 'Requiere laboratorio',
    },
    {
        id: '2026-6-ing',
        yearId: '2026',
        gradeId: '6',
        area: 'Inglés',
        subject: 'Inglés',
        weeklyHours: 3,
        shift: 'Mañana',
        note: null,
    },
];
export function studyPlanYearOptions() {
    return academicYears.map((year) => ({
        value: year.id,
        label: String(year.year),
    }));
}
export function currentStudyPlanYear() {
    return academicYears.find((year) => year.current) ?? academicYears[0];
}
export function formatAvailableYears(years = academicYears) {
    return years
        .map((year) => `${year.year} — ${labelYearStatus(year.status)}`)
        .join(' · ');
}
export function findStudyPlanGrade(gradeId) {
    return studyPlanGrades.find((grade) => grade.id === gradeId) ?? studyPlanGrades[0];
}
export function filterStudyPlanEntries(entries, filters) {
    const query = filters.query.trim().toLowerCase();
    return entries.filter((entry) => {
        if (entry.yearId !== filters.yearId || entry.gradeId !== filters.gradeId) {
            return false;
        }
        if (filters.area && entry.area !== filters.area) {
            return false;
        }
        if (!query) {
            return true;
        }
        return entry.subject.toLowerCase().includes(query);
    });
}
export function configuredWeeklyHours(entries) {
    return entries.reduce((total, entry) => total + entry.weeklyHours, 0);
}
export const studyPlanShifts = ['Mañana', 'Tarde'];
export const planSubjectFormHint = 'La asignatura se selecciona desde M-07. El área se deriva automáticamente. Las horas corresponden al Plan de Estudios del grado.';
export const derivedAreaLabel = 'Derivada de la asignatura';
export function studyPlanShiftOptions(current) {
    const shifts = current && !studyPlanShifts.includes(current)
        ? [current, ...studyPlanShifts]
        : [...studyPlanShifts];
    return shifts.map((shift) => ({ value: shift, label: shift }));
}
