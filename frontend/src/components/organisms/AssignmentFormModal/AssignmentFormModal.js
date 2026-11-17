import { jsxs as _jsxs, jsx as _jsx, Fragment as _Fragment } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listarAsignacionesPorDocente, listarIdoneidadesPorDocente } from '../../../api';
import { assignmentKindHint, assignmentRestrictionWarning, assignmentTeacherPriorityHint, hoursStatusCopy, namedTeacherValue, parseTeacherSelection, subjectPlanHint, teacherCompatibilityHint, } from '../../../content/academicAssignment';
import { isIdoneidadVigente, resolveAreaName } from '../../../content/teacherProfile';
import { formatTeacherName, labelEstado } from '../../../content/teachers';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import { TextArea } from '../../atoms/TextArea';
import { FormField } from '../../molecules/FormField';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import { AssignmentKindModal } from '../AssignmentKindModal';
import styles from './AssignmentFormModal.module.css';
const requiredChoiceMessage = 'Seleccione una opción.';
const hoursMessage = 'Ingresa las horas semanales asignadas.';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function initialTeacherValue(row, teachers) {
    if (row?.teacherId && teachers.some((teacher) => teacher.id === row.teacherId)) {
        return row.teacherId;
    }
    if (row?.teacherName) {
        const match = teachers.find((teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase());
        return match?.id ?? namedTeacherValue(row.teacherName);
    }
    return '';
}
export function AssignmentFormModal({ open, mode, year, restrictionCounts, groupCode, gradeLabel, subjects, teachers, areas, row, saving = false, error = '', onClose, onSubmit, }) {
    const navigate = useNavigate();
    const editing = mode === 'edit';
    const initialSubject = row?.key ?? subjects.find((item) => !item.assigned)?.key ?? subjects[0]?.key ?? '';
    const [values, setValues] = useState(() => ({
        subjectKey: initialSubject,
        teacherValue: initialTeacherValue(row, teachers),
        hours: row
            ? String(row.assignedHours)
            : String(subjects.find((item) => item.key === initialSubject)?.requiredHours ?? ''),
        kind: row?.kind ?? 'PRINCIPAL',
        note: row?.note ?? (editing ? `Asignación curricular del grupo ${groupCode}` : ''),
    }));
    const [attempted, setAttempted] = useState(false);
    const [kindOpen, setKindOpen] = useState(false);
    const [hasValidIdoneidad, setHasValidIdoneidad] = useState(false);
    const [teacherLoad, setTeacherLoad] = useState('');
    const formId = `assignment-${mode}-form`;
    const selected = subjects.find((item) => item.key === values.subjectKey);
    const hoursValue = Number(values.hours);
    const hoursInvalid = !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1;
    const hoursCopy = selected ? hoursStatusCopy(selected.requiredHours, hoursInvalid ? 0 : hoursValue) : null;
    const teacherOptions = teacherSelectOptions(teachers, row);
    const selectedTeacher = resolveSelectedTeacher(values.teacherValue, teachers);
    const teacherArea = selectedTeacher
        ? resolveAreaName(selectedTeacher.areaNombramientoId, areas)
        : '';
    const restrictionCount = selectedTeacher
        ? (restrictionCounts?.[selectedTeacher.id] ?? 0)
        : 0;
    const teacherHint = [
        selectedTeacher
            ? teacherCompatibilityHint(`Docente ${labelEstado(selectedTeacher.estado).toLowerCase()}`, teacherArea, selected?.area ?? '', hasValidIdoneidad)
            : '',
        values.teacherValue ? assignmentTeacherPriorityHint : '',
        teacherLoad,
    ]
        .filter(Boolean)
        .join(' ');
    useEffect(() => {
        const teacherId = parseTeacherSelection(values.teacherValue).teacherId;
        if (!teacherId || !selected?.area) {
            setHasValidIdoneidad(false);
            return;
        }
        const controller = new AbortController();
        listarIdoneidadesPorDocente(teacherId, controller.signal)
            .then((items) => {
            setHasValidIdoneidad(items.some((item) => {
                if (!isIdoneidadVigente(item)) {
                    return false;
                }
                const areaName = resolveAreaName(item.areaId, areas);
                return areaName.toLowerCase() === selected.area.toLowerCase();
            }));
        })
            .catch((cause) => {
            if (!isAbortError(cause)) {
                setHasValidIdoneidad(false);
            }
        });
        return () => controller.abort();
    }, [areas, selected?.area, values.teacherValue]);
    useEffect(() => {
        const teacherId = parseTeacherSelection(values.teacherValue).teacherId;
        if (!teacherId) {
            setTeacherLoad('');
            return;
        }
        const controller = new AbortController();
        listarAsignacionesPorDocente(teacherId, controller.signal)
            .then((items) => {
            const hours = items.reduce((total, item) => total + (item.horasSemanales || 0), 0);
            if (items.length === 0) {
                setTeacherLoad('Este docente no tiene asignaciones registradas.');
                return;
            }
            const label = items.length === 1 ? 'asignación' : 'asignaciones';
            setTeacherLoad(`Carga actual: ${hours} h semanales en ${items.length} ${label}.`);
        })
            .catch((cause) => {
            if (!isAbortError(cause)) {
                setTeacherLoad('');
            }
        });
        return () => controller.abort();
    }, [values.teacherValue]);
    const handleSubmit = (event) => {
        event.preventDefault();
        if (saving) {
            return;
        }
        setAttempted(true);
        if (!values.subjectKey || !values.teacherValue || hoursInvalid) {
            return;
        }
        const teacher = parseTeacherSelection(values.teacherValue);
        const catalogTeacher = teacher.teacherId
            ? teachers.find((item) => item.id === teacher.teacherId)
            : undefined;
        onSubmit({
            subjectKey: values.subjectKey,
            teacherId: teacher.teacherId,
            teacherName: catalogTeacher
                ? formatTeacherName(catalogTeacher)
                : teacher.teacherName,
            assignedHours: hoursValue,
            kind: values.kind,
            note: values.note.trim(),
        });
    };
    const patch = (partial) => {
        setValues((current) => ({ ...current, ...partial }));
    };
    const adjustHours = (delta) => {
        const current = hoursInvalid ? selected?.requiredHours ?? 1 : hoursValue;
        patch({ hours: String(Math.max(1, current + delta)) });
    };
    return (_jsxs(_Fragment, { children: [_jsx(Modal, { open: open, title: editing ? 'Editar asignación académica' : 'Nueva asignación académica', description: `Año lectivo ${year} · Grupo ${groupCode} · Grado ${gradeLabel}`, onClose: onClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: saving ? 'Guardando…' : editing ? 'Guardar cambios' : 'Guardar asignación', confirmType: "submit", disabled: saving, onCancel: onClose }), children: _jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [editing && selected ? (_jsxs("p", { className: styles.context, children: ["Asignatura: ", selected.subject, " \u00B7 Requeridas: ", selected.requiredHours, " h"] })) : null, _jsx(FormField, { id: `${formId}-kind`, label: "Tipo de asignaci\u00F3n", hint: assignmentKindHint, children: _jsx("button", { id: `${formId}-kind`, type: "button", className: styles.trigger, onClick: () => setKindOpen(true), children: labelKind(values.kind) }) }), editing ? null : (_jsx(SelectField, { id: `${formId}-subject`, label: "Asignatura", name: "subject", required: true, value: values.subjectKey, options: subjects.map((item) => ({
                                value: item.key,
                                label: item.subject,
                            })), hint: subjectPlanHint(gradeLabel, selected), error: attempted && !values.subjectKey ? requiredChoiceMessage : undefined, onChange: (subjectKey) => {
                                const next = subjects.find((item) => item.key === subjectKey);
                                patch({
                                    subjectKey,
                                    hours: String(next ? Math.max(next.requiredHours - next.assignedHours, 1) : ''),
                                });
                            } })), editing ? (_jsx(SelectField, { id: `${formId}-teacher`, label: "Docente", name: "teacher", required: true, value: values.teacherValue, options: teacherOptions, hint: teacherHint || undefined, error: attempted && !values.teacherValue ? requiredChoiceMessage : undefined, onChange: (teacherValue) => patch({ teacherValue }) })) : null, _jsxs("div", { className: styles.hoursBlock, children: [_jsx(TextField, { id: `${formId}-hours`, label: editing ? 'Horas asignadas' : 'Horas a asignar', name: "hours", required: true, inputMode: "numeric", placeholder: "Ej. 3", value: values.hours, error: attempted && hoursInvalid ? hoursMessage : undefined, onChange: (event) => patch({ hours: event.target.value.replace(/\D/g, '') }) }), editing && selected ? (_jsx(Button, { type: "button", variant: "outline", size: "sm", onClick: () => adjustHours(hoursValue > selected.requiredHours ? -1 : 1), children: hoursValue > selected.requiredHours ? '- 1 hora' : '+ 1 hora' })) : null, editing && hoursCopy ? (_jsx("p", { className: `${styles.status} ${styles[hoursCopy.tone]}`, children: hoursCopy.text })) : null] }), editing ? null : (_jsx(SelectField, { id: `${formId}-teacher`, label: "Docente", name: "teacher", required: true, value: values.teacherValue, options: teacherOptions, hint: teacherHint || undefined, error: attempted && !values.teacherValue ? requiredChoiceMessage : undefined, onChange: (teacherValue) => patch({ teacherValue }) })), _jsx(FormField, { id: `${formId}-note`, label: "Observaci\u00F3n", children: _jsx(TextArea, { id: `${formId}-note`, name: "note", rows: 3, placeholder: "Observaci\u00F3n opcional", value: values.note, onChange: (event) => patch({ note: event.target.value }) }) }), error ? _jsx("p", { className: styles.error, children: error }) : null, restrictionCount > 0 ? (_jsx("p", { className: styles.warning, children: assignmentRestrictionWarning })) : null, editing && selectedTeacher ? (_jsxs("div", { className: styles.links, children: [_jsx(Button, { type: "button", variant: "outline", size: "sm", onClick: () => navigate(`/dashboard/docentes/${selectedTeacher.id}`), children: "Ver perfil acad\u00E9mico" }), _jsx(Button, { type: "button", variant: "outline", size: "sm", onClick: () => navigate(`/dashboard/restricciones?docente=${selectedTeacher.id}`), children: "Ver restricciones" })] })) : null] }) }), _jsx(AssignmentKindModal, { open: kindOpen, onClose: () => setKindOpen(false), onSelect: (kind) => {
                    patch({ kind });
                    setKindOpen(false);
                } })] }));
}
function labelKind(kind) {
    if (kind === 'IDONEIDAD') {
        return 'Idoneidad';
    }
    if (kind === 'TRANSVERSAL') {
        return 'Transversal';
    }
    return 'Principal';
}
function teacherSelectOptions(teachers, row) {
    const options = [
        { value: '', label: 'Seleccionar docente' },
        ...teachers
            .filter((teacher) => teacher.estado === 'ACTIVO')
            .map((teacher) => ({
            value: teacher.id,
            label: formatTeacherName(teacher),
        })),
    ];
    if (row?.teacherName &&
        !teachers.some((teacher) => formatTeacherName(teacher).toLowerCase() === row.teacherName.toLowerCase())) {
        options.splice(1, 0, {
            value: namedTeacherValue(row.teacherName),
            label: row.teacherName,
        });
    }
    return options;
}
function resolveSelectedTeacher(value, teachers) {
    const parsed = parseTeacherSelection(value);
    if (parsed.teacherId) {
        return teachers.find((teacher) => teacher.id === parsed.teacherId);
    }
    if (parsed.teacherName) {
        return teachers.find((teacher) => formatTeacherName(teacher).toLowerCase() === parsed.teacherName.toLowerCase());
    }
    return undefined;
}
