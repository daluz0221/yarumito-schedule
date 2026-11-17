import { seedPlacements } from './scheduleConstruction';
import { publishReadiness, scheduleNameForYear, } from './validations';
export const publicationVersion = 1;
export const publicationCopy = {
    pageTitle: 'Publicación del horario',
    pageSubtitle: 'Revisa el resultado final y publica la versión del horario académico.',
    summaryTitle: 'Resumen final',
    checksTitle: 'Comprobaciones antes de publicar',
    versionsTitle: 'Versiones del horario',
    currentVersion: 'v1 · Actual',
    previousTitle: 'Versiones anteriores',
    previousEmpty: 'No se muestran versiones anteriores publicadas en este ejemplo. Si existen, deben preservarse; el archivado automático de una versión previa queda pendiente de regla de negocio explícita.',
    logTitle: 'Registro de publicación',
    logEmpty: 'Aún no hay registros de publicación para este horario.',
    logNote: 'El backend aún no conserva la publicación. Este registro queda en la sesión actual.',
    publish: 'Publicar horario',
    published: 'Publicado',
    seeWarnings: 'Ver advertencias',
    seeValidations: 'Ver validaciones',
    noBlocking: 'Sin errores bloqueantes',
    blocking: 'Hay errores bloqueantes',
    warningsNote: 'Existen advertencias pendientes. Revisa su impacto antes de publicar.',
    blockingNote: 'Existen errores pendientes. Corrige el horario y vuelve a validar antes de publicar.',
    readyNote: 'No hay errores ni advertencias pendientes. Puedes publicar esta versión.',
    warningsTitle: 'Advertencias pendientes',
    warningsLead: (count) => `Este horario no contiene errores bloqueantes, pero mantiene ${count} ${count === 1 ? 'advertencia' : 'advertencias'}.`,
    warningsDisclaimer: 'Las advertencias no bloquean por sí mismas este flujo de publicación.',
    warningsReview: 'Volver a revisar',
    warningsPublish: 'Publicar de todas formas',
    confirmTitle: 'Publicar horario',
    confirmLead: (version, scheduleName, campus) => `Se publicará la versión ${version} del ${scheduleName} para la ${campus}.`,
    confirmEffects: [
        'El horario pasará de BORRADOR a PUBLICADO.',
        'Se registrará la fecha de publicación.',
        'Esta versión será disponible para consulta.',
        'Las versiones anteriores, si existen, deben preservarse.',
    ],
    confirmCancel: 'Cancelar',
    confirmAction: 'Publicar horario',
    blockedTitle: 'Publicación bloqueada',
    blockedLead: 'No es posible publicar mientras existan errores pendientes.',
    blockedHint: 'Vuelve a Validaciones y conflictos, corrige el horario y ejecuta una nueva validación.',
    blockedAction: 'Volver a validaciones',
    successTitle: 'Horario publicado',
    successLead: (version, scheduleName) => `La versión ${version} del ${scheduleName} fue publicada correctamente.`,
    successSeeSchedule: 'Ver horario',
    successClose: 'Cerrar',
};
export function publicationAuthor(user) {
    if (user?.rol === 'DOCENTE') {
        return 'Docente';
    }
    return 'Rector / Administrador';
}
export function buildPublicationView({ year, placements = seedPlacements, conflicts, author, published, campusName, }) {
    const yearPlacements = placements.filter((item) => item.yearId === year.id);
    const pending = conflicts.filter((item) => item.status === 'PENDIENTE');
    const errors = pending.filter((item) => item.severity === 'ERROR').length;
    const warnings = pending.filter((item) => item.severity === 'ADVERTENCIA').length;
    const scheduleName = scheduleNameForYear(year);
    const campusFull = campusName.trim() || 'Sede no disponible';
    return {
        scheduleName,
        yearLabel: String(year.year),
        campus: campusFull.replace(/^Sede\s+/i, ''),
        campusFull,
        version: publicationVersion,
        status: published ? 'PUBLICADO' : 'BORRADOR',
        groups: new Set(yearPlacements.map((item) => item.groupId)).size,
        classes: yearPlacements.length,
        errors,
        warnings,
        author,
        publicationLabel: published ? 'Publicado' : 'Pendiente',
        readiness: publishReadiness(errors, warnings),
        checks: publicationChecks(pending),
        versionCaption: published
            ? `${scheduleName} · publicado en esta sesión`
            : `${scheduleName} · pendiente de publicación`,
    };
}
export function pendingPublicationWarnings(conflicts) {
    return conflicts
        .filter((item) => item.status === 'PENDIENTE' && item.severity === 'ADVERTENCIA')
        .map((item) => ({
        id: item.id,
        title: publicationWarningTitle(item),
    }));
}
export function publicationWarningTitle(conflict) {
    if (conflict.kind === 'capacity') {
        return conflict.classroom ? `Sobreaforo — ${conflict.classroom}` : 'Sobreaforo';
    }
    if (conflict.kind === 'preference') {
        return 'Preferencia docente no satisfecha';
    }
    if (conflict.kind === 'lab-required') {
        return conflict.subject ? `Aula no requerida — ${conflict.subject}` : 'Aula no requerida';
    }
    if (conflict.kind === 'incomplete-hours') {
        return conflict.subject && conflict.groupCode
            ? `${conflict.subject} — grupo ${conflict.groupCode}`
            : conflict.message;
    }
    return conflict.message;
}
export function publicationChecks(pending) {
    const hasError = (...kinds) => pending.some((item) => item.severity === 'ERROR' && kinds.includes(item.kind));
    const incomplete = pending.some((item) => item.kind === 'incomplete-hours');
    return [
        {
            id: 'assignments',
            label: 'Asignaciones académicas programadas',
            passed: !incomplete,
        },
        {
            id: 'teacher',
            label: 'Sin conflictos de docente',
            passed: !hasError('teacher-busy', 'teacher-unavailable'),
        },
        {
            id: 'group',
            label: 'Sin conflictos de grupo',
            passed: !hasError('group-busy'),
        },
        {
            id: 'classroom',
            label: 'Sin conflictos de aula',
            passed: !hasError('classroom-busy'),
        },
    ];
}
