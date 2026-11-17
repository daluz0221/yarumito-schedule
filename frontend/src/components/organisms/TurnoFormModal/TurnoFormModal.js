import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarTurno, ApiError, horaCorta, registrarTurno } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { TextField } from '../../molecules/TextField';
import styles from './TurnoFormModal.module.css';
const duplicateMessage = 'Ya existe un turno con ese nombre';
const rangeMessage = 'La hora de inicio debe ser anterior a la hora de finalización';
const structureMessage = 'Las clases antes del descanso no pueden superar las clases por día';
const emptyValues = {
    nombre: '',
    horaInicio: '',
    horaFin: '',
    duracionClaseMinutos: '55',
    clasesPorDia: '6',
    clasesAntesDeDescanso: '3',
    duracionDescansoMinutos: '30',
};
function normalizarNombre(nombre) {
    return nombre.trim().replace(/\s+/g, ' ');
}
function valuesFromTurno(turno) {
    if (!turno) {
        return emptyValues;
    }
    return {
        nombre: turno.nombre,
        horaInicio: horaCorta(turno.horaInicio),
        horaFin: horaCorta(turno.horaFin),
        duracionClaseMinutos: String(turno.duracionClaseMinutos),
        clasesPorDia: String(turno.clasesPorDia),
        clasesAntesDeDescanso: String(turno.clasesAntesDeDescanso),
        duracionDescansoMinutos: String(turno.duracionDescansoMinutos),
    };
}
function nombreOcupado(turnos, nombre, turnoId) {
    const normalizado = normalizarNombre(nombre).toLowerCase();
    if (!normalizado) {
        return false;
    }
    return turnos.some((turno) => turno.id !== turnoId && normalizarNombre(turno.nombre).toLowerCase() === normalizado);
}
function entero(value) {
    return value === '' ? Number.NaN : Number(value);
}
function TurnoFormFields({ mode, turnos, turno, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const [values, setValues] = useState(() => mode === 'edit' ? valuesFromTurno(turno) : emptyValues);
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `turno-${mode}-form`;
    const creating = mode === 'create';
    const nameTaken = nombreOcupado(turnos, values.nombre, turno?.id);
    const rangeInvalid = Boolean(values.horaInicio && values.horaFin && values.horaInicio >= values.horaFin);
    const clases = entero(values.clasesPorDia);
    const antes = entero(values.clasesAntesDeDescanso);
    const structureInvalid = Number.isFinite(clases) && Number.isFinite(antes) && antes > clases;
    const durationInvalid = entero(values.duracionClaseMinutos) < 1;
    const classesInvalid = clases < 1;
    const breakInvalid = entero(values.duracionDescansoMinutos) < 0 || values.duracionDescansoMinutos === '';
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const setNumber = (field) => (value) => {
        setValues((current) => ({ ...current, [field]: value.replace(/\D/g, '') }));
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting ||
            !normalizarNombre(values.nombre) ||
            !values.horaInicio ||
            !values.horaFin ||
            nameTaken ||
            rangeInvalid ||
            structureInvalid ||
            durationInvalid ||
            classesInvalid ||
            breakInvalid ||
            !Number.isFinite(antes)) {
            return;
        }
        if (!creating && !turno?.id) {
            setSubmitError('No se encontró el turno a actualizar.');
            return;
        }
        const payload = {
            nombre: normalizarNombre(values.nombre),
            horaInicio: values.horaInicio,
            horaFin: values.horaFin,
            duracionClaseMinutos: entero(values.duracionClaseMinutos),
            clasesPorDia: clases,
            clasesAntesDeDescanso: antes,
            duracionDescansoMinutos: entero(values.duracionDescansoMinutos),
        };
        setBusy(true);
        try {
            if (creating) {
                onCreated?.(await registrarTurno(payload));
            }
            else if (turno) {
                onUpdated?.(await actualizarTurno(turno.id, payload));
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar el turno.'
                    : 'No se pudo actualizar el turno.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsx(TextField, { id: `${formId}-name`, label: "Nombre del turno", required: true, maxLength: 50, disabled: submitting, placeholder: "Ejemplo: Ma\u00F1ana", value: values.nombre, error: nameTaken
                    ? duplicateMessage
                    : attempted && !normalizarNombre(values.nombre)
                        ? 'El nombre del turno es obligatorio'
                        : undefined, onChange: (event) => setValues((current) => ({ ...current, nombre: event.target.value })) }), _jsxs("div", { className: styles.grid, children: [_jsx(TextField, { id: `${formId}-start`, label: "Hora de inicio", type: "time", required: true, disabled: submitting, value: values.horaInicio, error: rangeInvalid
                            ? rangeMessage
                            : attempted && !values.horaInicio
                                ? 'La hora de inicio es obligatoria'
                                : undefined, onChange: (event) => setValues((current) => ({ ...current, horaInicio: event.target.value })) }), _jsx(TextField, { id: `${formId}-end`, label: "Hora de finalizaci\u00F3n", type: "time", required: true, disabled: submitting, value: values.horaFin, error: attempted && !values.horaFin ? 'La hora de finalización es obligatoria' : undefined, onChange: (event) => setValues((current) => ({ ...current, horaFin: event.target.value })) }), _jsx(TextField, { id: `${formId}-class`, label: "Duraci\u00F3n de la clase (min)", inputMode: "numeric", required: true, disabled: submitting, value: values.duracionClaseMinutos, error: attempted && durationInvalid ? 'La duración de la clase debe ser mayor que cero' : undefined, onChange: (event) => setNumber('duracionClaseMinutos')(event.target.value) }), _jsx(TextField, { id: `${formId}-per-day`, label: "Clases por d\u00EDa", inputMode: "numeric", required: true, disabled: submitting, value: values.clasesPorDia, error: attempted && classesInvalid ? 'La cantidad de clases por día debe ser mayor que cero' : undefined, onChange: (event) => setNumber('clasesPorDia')(event.target.value) }), _jsx(TextField, { id: `${formId}-before-break`, label: "Clases antes del descanso", inputMode: "numeric", required: true, disabled: submitting, value: values.clasesAntesDeDescanso, error: structureInvalid
                            ? structureMessage
                            : attempted && !Number.isFinite(antes)
                                ? 'Indica las clases antes del descanso'
                                : undefined, onChange: (event) => setNumber('clasesAntesDeDescanso')(event.target.value) }), _jsx(TextField, { id: `${formId}-break`, label: "Descanso (min)", inputMode: "numeric", required: true, disabled: submitting, value: values.duracionDescansoMinutos, error: attempted && breakInvalid ? 'La duración del descanso no puede ser negativa' : undefined, onChange: (event) => setNumber('duracionDescansoMinutos')(event.target.value) })] }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: "La hora de inicio debe ser anterior a la de finalizaci\u00F3n. Las clases antes del descanso no pueden superar las clases del d\u00EDa." })] }));
}
export function TurnoFormModal({ open, mode, turnos, turno, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(turno));
    const formId = `turno-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: creating ? 'Nuevo turno' : 'Editar turno', description: creating
            ? 'Registra una jornada de la institución.'
            : 'Actualiza el horario y la estructura del turno.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar turno'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando turno..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(TurnoFormFields, { mode: mode, turnos: turnos, turno: turno, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, creating ? 'create' : turno?.id)) : (_jsx("p", { className: styles.status, children: "Cargando turno..." })) }));
}
