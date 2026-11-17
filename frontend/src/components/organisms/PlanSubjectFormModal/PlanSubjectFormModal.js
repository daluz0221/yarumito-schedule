import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { actualizarPlanEstudios, ApiError, listarAreas, listarAsignaturas, listarCatalogoTurnos, registrarPlanEstudios, } from '../../../api';
import { derivedAreaLabel } from '../../../content/studyPlan';
import { TextArea } from '../../atoms/TextArea';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { FormField } from '../../molecules/FormField';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './PlanSubjectFormModal.module.css';
const requiredChoiceMessage = 'Seleccione una opción.';
const hoursMessage = 'Ingresa las horas semanales.';
const duplicateCreateMessage = 'La asignatura ya está registrada para ese grado en el año escolar indicado';
const duplicateEditMessage = 'Ya existe otro registro de esa asignatura para ese grado en el año escolar indicado';
function isAbortError(error) {
    return error instanceof DOMException && error.name === 'AbortError';
}
function createValues() {
    return {
        subjectId: '',
        hours: '',
        turnoId: '',
        note: '',
    };
}
function valuesFromPlan(plan) {
    return {
        subjectId: plan.asignaturaId,
        hours: String(plan.horasSemanales),
        turnoId: plan.turnoId ?? '',
        note: plan.observacion ?? '',
    };
}
function asignaturaOcupada(planes, asignaturaId, planId) {
    return planes.some((plan) => plan.id !== planId && plan.asignaturaId === asignaturaId);
}
function PlanSubjectFormFields({ mode, yearId, gradeId, planes, plan, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const [catalog, setCatalog] = useState([]);
    const [turnos, setTurnos] = useState([]);
    const [values, setValues] = useState(() => mode === 'edit' && plan ? valuesFromPlan(plan) : createValues());
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `plan-subject-${mode}-form`;
    const creating = mode === 'create';
    const selected = catalog.find((item) => item.id === values.subjectId);
    const areaName = selected?.areaNombre ?? (plan?.asignaturaId === values.subjectId ? '' : '');
    const hoursValue = Number(values.hours);
    const hoursInvalid = !values.hours || !Number.isInteger(hoursValue) || hoursValue < 1;
    const duplicate = values.subjectId
        ? asignaturaOcupada(planes, values.subjectId, plan?.id)
        : false;
    const duplicateMessage = creating ? duplicateCreateMessage : duplicateEditMessage;
    const nameCounts = catalog.reduce((counts, item) => {
        counts[item.nombre] = (counts[item.nombre] ?? 0) + 1;
        return counts;
    }, {});
    const subjectOptions = [
        { value: '', label: 'Seleccionar asignatura' },
        ...(plan && !catalog.some((item) => item.id === plan.asignaturaId)
            ? [{ value: plan.asignaturaId, label: plan.asignaturaNombre }]
            : []),
        ...catalog.map((item) => ({
            value: item.id,
            label: nameCounts[item.nombre] > 1 ? `${item.nombre} (${item.areaNombre})` : item.nombre,
        })),
    ];
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    useEffect(() => {
        const controller = new AbortController();
        listarAreas(controller.signal)
            .then(async (areas) => {
            const groups = await Promise.all(areas.map((area) => listarAsignaturas(area.id, controller.signal, { activa: true }).then((subjects) => subjects.map((subject) => ({
                id: subject.id,
                nombre: subject.nombre,
                areaNombre: area.nombre,
            })))));
            if (!controller.signal.aborted) {
                setCatalog(groups.flat().sort((left, right) => left.nombre.localeCompare(right.nombre, 'es')));
            }
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setCatalog([]);
        });
        return () => controller.abort();
    }, []);
    useEffect(() => {
        const controller = new AbortController();
        listarCatalogoTurnos(controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setTurnos(items);
            }
        })
            .catch((cause) => {
            if (isAbortError(cause) || controller.signal.aborted) {
                return;
            }
            setTurnos([]);
        });
        return () => controller.abort();
    }, []);
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting || !values.subjectId || hoursInvalid || duplicate) {
            return;
        }
        if (!creating && !plan?.id) {
            setSubmitError('No se encontró el registro del plan a actualizar.');
            return;
        }
        const payload = {
            anioEscolarId: yearId,
            gradoId: gradeId,
            asignaturaId: values.subjectId,
            horasSemanales: hoursValue,
            turnoId: values.turnoId || null,
            observacion: values.note.trim() || null,
        };
        setBusy(true);
        try {
            if (creating) {
                const created = await registrarPlanEstudios(payload);
                onCreated?.(created);
            }
            else if (plan) {
                const updated = await actualizarPlanEstudios(plan.id, payload);
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar la asignatura en el plan. Inténtalo de nuevo.'
                    : 'No se pudo actualizar el plan de estudios. Inténtalo de nuevo.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsxs("div", { className: styles.grid, children: [_jsx(SelectField, { id: `${formId}-subject`, label: "Asignatura", name: "subject", required: true, disabled: submitting, value: values.subjectId, options: subjectOptions, error: attempted && !values.subjectId
                            ? requiredChoiceMessage
                            : duplicate
                                ? duplicateMessage
                                : undefined, onChange: (subjectId) => setValues((current) => ({ ...current, subjectId })) }), _jsx(TextField, { id: `${formId}-area`, label: "\u00C1rea", name: "area", disabled: true, className: areaName ? styles.locked : undefined, placeholder: derivedAreaLabel, value: areaName, onChange: () => undefined }), _jsx(TextField, { id: `${formId}-hours`, label: "Horas semanales", name: "hours", required: true, inputMode: "numeric", disabled: submitting, placeholder: "Ej. 4", value: values.hours, error: attempted && hoursInvalid ? hoursMessage : undefined, onChange: (event) => setValues((current) => ({
                            ...current,
                            hours: event.target.value.replace(/\D/g, ''),
                        })) }), _jsx(SelectField, { id: `${formId}-shift`, label: "Turno", name: "shift", disabled: submitting, value: values.turnoId, options: [
                            { value: '', label: 'Sin asignar' },
                            ...turnos.map((turno) => ({ value: turno.id, label: turno.nombre })),
                            ...(values.turnoId && !turnos.some((turno) => turno.id === values.turnoId)
                                ? [{ value: values.turnoId, label: 'Turno actual' }]
                                : []),
                        ], onChange: (turnoId) => setValues((current) => ({ ...current, turnoId })) })] }), _jsx(FormField, { id: `${formId}-note`, label: "Observaci\u00F3n", children: _jsx(TextArea, { id: `${formId}-note`, name: "note", rows: 3, disabled: submitting, placeholder: "Observaci\u00F3n opcional", value: values.note, onChange: (event) => setValues((current) => ({ ...current, note: event.target.value })) }) }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: "El \u00E1rea sale de la asignatura. Una asignatura no puede repetirse en el mismo a\u00F1o y grado. El turno es opcional." })] }));
}
export function PlanSubjectFormModal({ open, mode, year, yearId, gradeId, gradeLabel, planes, plan, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(plan));
    const formId = `plan-subject-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: creating ? 'Agregar asignatura al plan' : 'Editar asignatura del plan', description: creating
            ? `Año lectivo: ${year} · Grado: ${gradeLabel}`
            : `Año lectivo ${year} · Grado ${gradeLabel}. Los datos se cargan desde el servidor.`, onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Agregar al plan'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando el registro del plan..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(PlanSubjectFormFields, { mode: mode, yearId: yearId, gradeId: gradeId, planes: planes, plan: plan, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, mode === 'edit' ? plan?.id : 'create')) : (_jsx("p", { className: styles.status, children: "Cargando el registro del plan..." })) }));
}
