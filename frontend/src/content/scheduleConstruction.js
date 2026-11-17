import { curricularSubjects, planEntriesForGroup, } from './academicAssignment';
import { studyPlanEntries } from './studyPlan';
import { availabilityDays, availabilityStatus, morningBands, } from './teacherRestrictions';
export const scheduleShiftOptions = [{ value: 'principal', label: 'Principal' }];
export const scheduleViewOptions = [{ value: 'grupo', label: 'Por grupo' }];
export const scheduleShiftLabel = 'Principal';
export const scheduleStatusLabel = 'BORRADOR';
export const scheduleClassrooms = [
    { value: 'Aula 201', label: 'Aula 201', capacity: 30 },
    { value: 'Aula 202', label: 'Aula 202', capacity: 32 },
    { value: 'Laboratorio', label: 'Laboratorio', capacity: 40 },
    { value: 'Lab. 1', label: 'Lab. 1', capacity: 24 },
];
export function scheduleDayOptions() {
    return availabilityDays.map((day) => ({ value: day, label: day }));
}
export function scheduleBandOptions() {
    return classBands().map((band) => ({
        value: band.id,
        label: band.label.replace(/\b(\d):/g, '0$1:'),
    }));
}
export function scheduleClassroomOptions() {
    return scheduleClassrooms.map((room) => ({ value: room.value, label: room.label }));
}
export function classroomCapacity(classroom) {
    return scheduleClassrooms.find((room) => sameRoom(room.value, classroom))?.capacity ?? 0;
}
// Referencia local de la validación. La construcción de horario carga la
// grilla desde GET /api/horarios/grupos/{grupoId}.
export const seedPlacements = [
    placement('601-lun-0630', '601', 'Lunes', '0630', '2026-6-mat', 'Carlos Pérez', 'Aula 201'),
    placement('601-mar-0725', '601', 'Martes', '0725', '2026-6-bio', 'Diana Marcela Ruiz', 'Laboratorio'),
    placement('601-jue-0945', '601', 'Jueves', '0945', '2026-6-mat', 'Carlos Pérez', 'Aula 201'),
    placement('601-vie-1040', '601', 'Viernes', '1040', '2026-6-mat', 'Carlos Pérez', 'Aula 201'),
    placement('602-jue-0945', '602', 'Jueves', '0945', '2026-6-mat', 'Carlos Pérez', 'Aula 202'),
];
export function classBands() {
    return morningBands.filter((band) => band.kind === 'class');
}
export function breakBand() {
    return morningBands.find((band) => band.kind === 'break');
}
export function scheduleSubject(subjectKey) {
    return studyPlanEntries.find((entry) => entry.id === subjectKey);
}
export function defaultClassroom(note) {
    if (note && /laborat/i.test(note)) {
        return 'Laboratorio';
    }
    return 'Aula 201';
}
export function blockKey(day, bandId) {
    return `${day}-${bandId}`;
}
export function assessPlacement(placement, placements, requiresLab, students = 0) {
    const availability = availabilityStatus(placement.yearId, placement.teacherName, placement.bandId, placement.day);
    const sameSlot = placements.filter((other) => other.id !== placement.id &&
        other.yearId === placement.yearId &&
        other.day === placement.day &&
        other.bandId === placement.bandId);
    const sharedTeacher = sameSlot.some((other) => other.groupId !== placement.groupId && sameTeacher(other.teacherName, placement.teacherName));
    const sharedRoom = sameSlot.some((other) => sameRoom(other.classroom, placement.classroom));
    if (availability === 'NO_DISPONIBLE') {
        return {
            tone: 'conflict',
            reason: 'El docente no está disponible en esta franja.',
        };
    }
    if (sharedTeacher) {
        return {
            tone: 'conflict',
            reason: 'El docente ya tiene clase en otro grupo a esta hora.',
        };
    }
    if (sharedRoom) {
        return {
            tone: 'conflict',
            reason: `${placement.classroom} ya está ocupada en esta franja.`,
        };
    }
    const warnings = [
        requiresLab && !isLaboratory(placement.classroom) ? 'La asignatura requiere laboratorio.' : '',
        availability === 'PREFERENCIA' ? 'El docente registró esta franja como preferencia.' : '',
        exceedsClassroomCapacity(placement.classroom, students)
            ? 'La cantidad de estudiantes del grupo supera la capacidad del aula.'
            : '',
    ].filter(Boolean);
    if (warnings.length > 0) {
        return { tone: 'warning', reason: warnings.join(' ') };
    }
    return { tone: 'ok', reason: '' };
}
export function inspectScheduleChange(draft, placements, students) {
    const sameSlot = placements.filter((other) => other.id !== draft.id &&
        other.yearId === draft.yearId &&
        other.day === draft.day &&
        other.bandId === draft.bandId);
    const slotTaken = sameSlot.some((other) => other.groupId === draft.groupId);
    const teacherBusy = sameSlot.some((other) => other.groupId !== draft.groupId && sameTeacher(other.teacherName, draft.teacherName));
    const classroomBusy = sameSlot.some((other) => sameRoom(other.classroom, draft.classroom));
    const unavailable = availabilityStatus(draft.yearId, draft.teacherName, draft.bandId, draft.day) === 'NO_DISPONIBLE';
    if (slotTaken) {
        return { blocking: { kind: 'slot-taken' }, capacity: false };
    }
    if (teacherBusy) {
        return {
            blocking: { kind: 'teacher-busy', teacherName: draft.teacherName },
            capacity: false,
        };
    }
    if (classroomBusy) {
        return {
            blocking: { kind: 'classroom-busy', classroom: draft.classroom },
            capacity: false,
        };
    }
    if (unavailable) {
        return {
            blocking: {
                kind: 'teacher-unavailable',
                teacherName: draft.teacherName,
            },
            capacity: false,
        };
    }
    return {
        blocking: null,
        capacity: exceedsClassroomCapacity(draft.classroom, students),
    };
}
export function blocksForGroup(placements, yearId, groupId, students = 0) {
    const blocks = new Map();
    placements
        .filter((item) => item.yearId === yearId && item.groupId === groupId)
        .forEach((item) => {
        const subject = scheduleSubject(item.subjectKey);
        const assessment = assessPlacement(item, placements, Boolean(subject?.note && /laborat/i.test(subject.note)), students);
        blocks.set(blockKey(item.day, item.bandId), {
            ...item,
            subject: subject?.subject ?? 'Asignatura',
            tone: assessment.tone,
            reason: assessment.reason,
        });
    });
    return blocks;
}
export function pendingPlacements(rows, placements, yearId, groupId) {
    return rows
        .filter((row) => row.assigned)
        .map((row) => {
        const placed = placements.filter((item) => item.yearId === yearId &&
            item.groupId === groupId &&
            item.subjectKey === row.key).length;
        return {
            key: row.key,
            subject: row.subject,
            teacherName: row.teacherName,
            pendingHours: Math.max(row.assignedHours - placed, 0),
            note: scheduleSubject(row.key)?.note ?? null,
        };
    })
        .filter((row) => row.pendingHours > 0);
}
export function scheduleMetrics(rows, placements, blocks, yearId, groupId) {
    const assigned = rows.filter((row) => row.assigned);
    const groupPlacements = placements.filter((item) => item.yearId === yearId && item.groupId === groupId);
    const programmed = assigned.filter((row) => groupPlacements.some((item) => item.subjectKey === row.key)).length;
    const assignedHours = assigned.reduce((total, row) => total + row.assignedHours, 0);
    const placedHours = groupPlacements.filter((item) => assigned.some((row) => row.key === item.subjectKey)).length;
    const tones = [...blocks.values()];
    return {
        programmed,
        assignedCount: assigned.length,
        placedHours,
        assignedHours,
        pendingHours: Math.max(assignedHours - placedHours, 0),
        conflicts: tones.filter((item) => item.tone === 'conflict').length,
        warnings: tones.filter((item) => item.tone === 'warning').length,
    };
}
export function curricularRowsSource(yearId, group) {
    return curricularSubjects(planEntriesForGroup(yearId, group));
}
function placement(id, groupId, day, bandId, subjectKey, teacherName, classroom) {
    return {
        id,
        yearId: '2026',
        groupId,
        day,
        bandId,
        subjectKey,
        teacherName,
        classroom,
    };
}
function sameTeacher(left, right) {
    return normalize(left) === normalize(right);
}
function sameRoom(left, right) {
    return normalize(left) === normalize(right);
}
function exceedsClassroomCapacity(classroom, students) {
    const capacity = classroomCapacity(classroom);
    return capacity > 0 && students > capacity;
}
function isLaboratory(classroom) {
    return /lab/i.test(classroom);
}
function normalize(value) {
    return value
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}
