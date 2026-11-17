import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarAnioEscolar, ApiError, registrarAnioEscolar, } from '../../../api';
import { academicYearStatusOptions, labelYearStatus, } from '../../../content/academicYear';
import { Checkbox } from '../../atoms/Checkbox';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './AcademicYearFormModal.module.css';
const ESTADOS = ['PLANEACION', 'ACTIVO', 'CERRADO'];
function esEstado(value) {
    return ESTADOS.includes(value);
}
function fechaIso(value) {
    const iso = /^(\d{4})-(\d{2})-(\d{2})/.exec(value ?? '');
    if (iso) {
        return `${iso[1]}-${iso[2]}-${iso[3]}`;
    }
    const local = /^(\d{2})\/(\d{2})\/(\d{4})/.exec(value ?? '');
    if (local) {
        return `${local[3]}-${local[2]}-${local[1]}`;
    }
    return '';
}
function sugerido(years) {
    const mayor = years.reduce((maximo, item) => Math.max(maximo, item.year), 0);
    const anio = mayor > 0 ? mayor + 1 : new Date().getFullYear();
    return {
        anio: String(anio),
        fechaInicio: `${anio}-01-19`,
        fechaFin: `${anio}-11-27`,
        estado: 'PLANEACION',
        esActual: false,
    };
}
function valoresDesdeAnio(anio) {
    return {
        anio: String(anio.anio),
        fechaInicio: fechaIso(anio.fechaInicio),
        fechaFin: fechaIso(anio.fechaFin),
        estado: esEstado(anio.estado) ? anio.estado : 'PLANEACION',
        esActual: Boolean(anio.esActual),
    };
}
function transicionPermitida(actual, siguiente) {
    if (actual === siguiente) {
        return actual !== 'CERRADO';
    }
    return ((actual === 'PLANEACION' && siguiente === 'ACTIVO') ||
        (actual === 'ACTIVO' && siguiente === 'CERRADO'));
}
function mensajeFormulario(values, years, anio) {
    const numero = Number(values.anio);
    if (!Number.isInteger(numero)) {
        return 'El año escolar es obligatorio';
    }
    const duplicado = years.some((item) => item.id !== anio?.id && item.year === numero);
    if (duplicado) {
        return anio
            ? `Ya existe otro año escolar registrado para ${numero}`
            : `Ya existe un año escolar registrado para ${numero}`;
    }
    if (values.fechaInicio && values.fechaFin && values.fechaInicio >= values.fechaFin) {
        return 'La fecha de inicio debe ser anterior a la fecha de finalización';
    }
    if (anio && esEstado(anio.estado) && anio.estado === 'CERRADO') {
        return 'Un año escolar cerrado no puede ser modificado';
    }
    if (anio && esEstado(anio.estado) && !transicionPermitida(anio.estado, values.estado)) {
        return `Transición de estado no permitida: ${anio.estado} -> ${values.estado}`;
    }
    if (values.esActual && values.estado !== 'ACTIVO') {
        return 'Solo un año escolar en estado ACTIVO puede ser marcado como actual';
    }
    if (values.estado === 'ACTIVO' && !values.esActual) {
        return 'Un año escolar en estado ACTIVO debe estar marcado como actual';
    }
    const otroActual = years.some((item) => item.current && item.id !== anio?.id);
    if (values.esActual && otroActual) {
        return anio
            ? 'Ya existe otro año escolar marcado como actual'
            : 'Ya existe un año escolar marcado como actual';
    }
    return '';
}
function AcademicYearFormFields({ mode, years, anio, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const [values, setValues] = useState(() => mode === 'edit' && anio ? valoresDesdeAnio(anio) : sugerido(years));
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `academic-year-${mode}-form`;
    const creating = mode === 'create';
    const aviso = mensajeFormulario(values, years, anio);
    const cerrado = anio?.estado === 'CERRADO';
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting || aviso || !values.fechaInicio || !values.fechaFin) {
            return;
        }
        if (!creating && !anio?.id) {
            setSubmitError('No se encontró el año escolar a actualizar.');
            return;
        }
        const payload = {
            anio: Number(values.anio),
            fechaInicio: values.fechaInicio,
            fechaFin: values.fechaFin,
            estado: values.estado,
            esActual: values.esActual,
        };
        setBusy(true);
        try {
            if (creating) {
                const created = await registrarAnioEscolar(payload);
                onCreated?.(created);
            }
            else if (anio) {
                const updated = await actualizarAnioEscolar(anio.id, payload);
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar el año escolar. Inténtalo de nuevo.'
                    : 'No se pudo actualizar el año escolar. Inténtalo de nuevo.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsx(TextField, { id: `${formId}-year`, label: "A\u00F1o", name: "anio", type: "number", required: true, min: 2000, max: 2100, disabled: submitting || cerrado, value: values.anio, error: aviso.startsWith('Ya existe') && aviso.includes('registrado')
                    ? aviso
                    : attempted && !Number.isInteger(Number(values.anio))
                        ? 'El año escolar es obligatorio'
                        : undefined, onChange: (event) => setValues((current) => ({ ...current, anio: event.target.value })) }), _jsxs("div", { className: styles.dates, children: [_jsx(TextField, { id: `${formId}-start`, label: "Fecha de inicio", name: "fechaInicio", type: "date", required: true, disabled: submitting || cerrado, value: values.fechaInicio, onChange: (event) => setValues((current) => ({
                            ...current,
                            fechaInicio: event.target.value,
                        })) }), _jsx(TextField, { id: `${formId}-end`, label: "Fecha de finalizaci\u00F3n", name: "fechaFin", type: "date", required: true, disabled: submitting || cerrado, value: values.fechaFin, error: values.fechaInicio &&
                            values.fechaFin &&
                            values.fechaInicio >= values.fechaFin
                            ? 'La fecha de inicio debe ser anterior a la fecha de finalización'
                            : undefined, onChange: (event) => setValues((current) => ({
                            ...current,
                            fechaFin: event.target.value,
                        })) })] }), _jsx(SelectField, { id: `${formId}-status`, label: "Estado", name: "estado", value: values.estado, options: academicYearStatusOptions, disabled: submitting || cerrado, error: aviso.startsWith('Transición') ||
                    aviso.startsWith('Solo un año') ||
                    aviso.startsWith('Un año escolar en estado ACTIVO') ||
                    aviso.startsWith('Un año escolar cerrado')
                    ? aviso
                    : undefined, hint: creating
                    ? 'Un año en planeación todavía no es el período actual.'
                    : `Estado guardado: ${anio ? labelYearStatus(esEstado(anio.estado) ? anio.estado : 'PLANEACION') : ''}. Solo se puede pasar de planeación a activo, y de activo a cerrado.`, onChange: (estado) => setValues((current) => ({
                    ...current,
                    estado: esEstado(estado) ? estado : current.estado,
                })) }), _jsxs("label", { className: styles.check, htmlFor: `${formId}-current`, children: [_jsx(Checkbox, { id: `${formId}-current`, name: "esActual", checked: values.esActual, disabled: submitting || cerrado, onChange: (event) => setValues((current) => ({
                            ...current,
                            esActual: event.target.checked,
                        })) }), "Este es el a\u00F1o escolar actual"] }), aviso.startsWith('Ya existe') && aviso.includes('actual') ? (_jsx("p", { className: styles.error, role: "alert", children: aviso })) : null, submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: "Solo puede haber un a\u00F1o actual, y tiene que estar activo. Un a\u00F1o cerrado queda solo para consulta." })] }));
}
export function AcademicYearFormModal({ open, mode, years, anio, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(anio));
    const formId = `academic-year-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: creating
            ? 'Crear nuevo año lectivo'
            : `Editar año lectivo ${anio?.anio ?? ''}`, description: creating
            ? 'Registra el período, sus fechas y si será el año actual.'
            : 'Actualiza el período seleccionado. Los datos se cargan desde el servidor.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Crear año lectivo'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando a\u00F1o escolar..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(AcademicYearFormFields, { mode: mode, years: years, anio: anio, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, mode === 'edit' ? anio?.id : 'create')) : (_jsx("p", { className: styles.status, children: "Cargando a\u00F1o escolar..." })) }));
}
