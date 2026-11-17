import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useState } from 'react';
import { scheduleBandOptions, scheduleClassroomOptions, scheduleDayOptions, } from '../../../content/scheduleConstruction';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import { SelectField } from '../../molecules/SelectField';
import styles from './ScheduleClassFormModal.module.css';
function aulaParaAsignacion(note, options, current) {
    const disponibles = options.filter((option) => option.value);
    if (note && /laborat/i.test(note)) {
        const laboratorio = disponibles.find((option) => /lab/i.test(option.label));
        if (laboratorio) {
            return laboratorio.value;
        }
    }
    if (disponibles.some((option) => option.value === current)) {
        return current;
    }
    return disponibles[0]?.value ?? '';
}
export function ScheduleClassFormModal({ open, mode, groupCode, subject, teacherName, pendingHours, assignments, day, bandId, classroom, subjectKey, bandOptions, classroomOptions, submitting = false, error = '', onClose, onSubmit, onRemove, }) {
    const creating = mode === 'create';
    const [values, setValues] = useState({ subjectKey, day, bandId, classroom });
    const formId = creating ? 'schedule-class-create' : 'schedule-class-edit';
    const selected = assignments.find((item) => item.key === values.subjectKey);
    const summarySubject = creating ? selected?.subject ?? subject : subject;
    const summaryTeacher = creating ? selected?.teacherName ?? teacherName : teacherName;
    const summaryHours = creating ? selected?.pendingHours ?? pendingHours : pendingHours;
    const assignmentOptions = assignments.map((item) => ({
        value: item.key,
        label: `${item.subject} · ${item.teacherName} · ${groupCode}`,
    }));
    const roomOptions = classroomOptions ?? scheduleClassroomOptions();
    const handleSubmit = (event) => {
        event.preventDefault();
        if (submitting || (creating && !selected) || !values.classroom) {
            return;
        }
        onSubmit({
            subjectKey: values.subjectKey,
            day: values.day,
            bandId: values.bandId,
            classroom: values.classroom,
        });
    };
    return (_jsx(Modal, { open: open, title: creating ? 'Programar clase' : 'Editar clase programada', onClose: submitting ? () => undefined : onClose, footer: _jsxs(_Fragment, { children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onClose, disabled: submitting, children: "Cancelar" }), creating ? null : (_jsx(Button, { variant: "outline", size: "sm", onClick: onRemove, disabled: submitting, children: "Quitar del horario" })), _jsx(Button, { type: "submit", form: formId, variant: "primary", size: "sm", disabled: submitting || (creating && !selected) || !values.classroom, children: submitting
                        ? creating
                            ? 'Programando…'
                            : 'Guardando…'
                        : creating
                            ? 'Programar clase'
                            : 'Guardar cambios' })] }), children: _jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsx("p", { className: styles.lead, children: creating
                        ? 'Ubica una asignación académica existente en el horario.'
                        : 'Modifica la ubicación temporal sin alterar la asignación académica.' }), _jsx("dl", { className: styles.summary, children: creating ? (_jsxs(_Fragment, { children: [_jsxs("div", { children: [_jsx("dt", { children: "Grupo" }), _jsx("dd", { children: groupCode })] }), _jsxs("div", { children: [_jsx("dt", { children: "Docente" }), _jsx("dd", { children: summaryTeacher || '—' })] }), _jsxs("div", { children: [_jsx("dt", { children: "Asignatura" }), _jsx("dd", { children: summarySubject || '—' })] }), _jsxs("div", { children: [_jsx("dt", { children: "Horas pendientes" }), _jsxs("dd", { children: [summaryHours, " h"] })] })] })) : (_jsxs(_Fragment, { children: [_jsxs("div", { children: [_jsx("dt", { children: "Asignatura" }), _jsx("dd", { children: summarySubject })] }), _jsxs("div", { children: [_jsx("dt", { children: "Docente" }), _jsx("dd", { children: summaryTeacher })] }), _jsxs("div", { children: [_jsx("dt", { children: "Grupo" }), _jsx("dd", { children: groupCode })] })] })) }), creating ? (_jsx(SelectField, { id: "schedule-assignment", label: "Asignaci\u00F3n acad\u00E9mica", name: "assignment", required: true, value: selected ? values.subjectKey : '', options: assignmentOptions.length > 0
                        ? assignmentOptions
                        : [{ value: '', label: 'Sin horas pendientes' }], disabled: assignmentOptions.length === 0 || submitting, onChange: (nextKey) => {
                        const next = assignments.find((item) => item.key === nextKey);
                        setValues((current) => ({
                            ...current,
                            subjectKey: nextKey,
                            classroom: aulaParaAsignacion(next?.note ?? null, roomOptions, current.classroom),
                        }));
                    } })) : null, _jsxs("div", { className: styles.pair, children: [_jsx(SelectField, { id: "schedule-day", label: "D\u00EDa", name: "day", required: true, value: values.day, options: scheduleDayOptions(), disabled: submitting, onChange: (nextDay) => setValues((current) => ({ ...current, day: nextDay })) }), _jsx(SelectField, { id: "schedule-band", label: "Franja horaria", name: "band", required: true, value: values.bandId, options: bandOptions ?? scheduleBandOptions(), disabled: submitting, onChange: (nextBand) => setValues((current) => ({ ...current, bandId: nextBand })) })] }), _jsx(SelectField, { id: "schedule-classroom", label: "Aula", name: "classroom", required: true, value: values.classroom, options: roomOptions.length > 0 ? roomOptions : [{ value: '', label: 'Sin aulas activas' }], disabled: submitting || roomOptions.length === 0, onChange: (nextRoom) => setValues((current) => ({ ...current, classroom: nextRoom })) }), creating ? (_jsx("p", { className: styles.hint, children: "La asignaci\u00F3n determina docente, asignatura y grupo. No se reasignan desde este m\u00F3dulo." })) : null, error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null] }) }));
}
