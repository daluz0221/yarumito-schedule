import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarAula, ApiError, opcionesCatalogoSedes, registrarAula, registrarNuevaVersionAula, tiposAula, } from '../../../api';
import { Checkbox } from '../../atoms/Checkbox';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './AulaFormModal.module.css';
const duplicateCreateMessage = 'Ya existe un aula activa con ese nombre en la sede indicada';
const duplicateEditMessage = 'Ya existe otra aula activa con ese nombre en la sede indicada';
const versionMessage = 'El cambio indicado no requiere una nueva versión del aula';
function normalizarNombre(nombre) {
    return nombre.trim().replace(/\s+/g, ' ');
}
function valuesFromAula(aula, sedeInicial) {
    if (!aula) {
        return {
            sedeId: sedeInicial,
            nombre: '',
            tipo: '',
            capacidad: '',
            activa: true,
        };
    }
    return {
        sedeId: aula.sedeId,
        nombre: aula.nombre,
        tipo: aula.tipo,
        capacidad: aula.capacidad == null ? '' : String(aula.capacidad),
        activa: aula.activa,
    };
}
function nombreOcupado(aulas, sedeId, nombre, aulaId) {
    const normalizado = normalizarNombre(nombre).toLowerCase();
    if (!normalizado || !sedeId) {
        return false;
    }
    return aulas.some((aula) => aula.activa &&
        aula.id !== aulaId &&
        aula.sedeId === sedeId &&
        normalizarNombre(aula.nombre).toLowerCase() === normalizado);
}
function sinCambioReal(aula, values) {
    return (normalizarNombre(aula.nombre).toLowerCase() === normalizarNombre(values.nombre).toLowerCase() &&
        aula.tipo === values.tipo &&
        aula.sedeId === values.sedeId);
}
function AulaFormFields({ mode, sedes, sedesError = '', aulas, aula, sedeInicial, onClose, onCreated, onUpdated, onVersioned, onSubmittingChange, }) {
    const [values, setValues] = useState(() => valuesFromAula(aula, sedeInicial));
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `aula-${mode}-form`;
    const sedeOptions = opcionesCatalogoSedes(sedes, sedesError);
    const sedeLista = Boolean(sedes?.length) && !sedesError;
    const tipoOptions = [{ value: '', label: 'Seleccione un tipo' }, ...tiposAula];
    const nombreTomado = nombreOcupado(aulas, values.sedeId, values.nombre, mode === 'create' ? undefined : aula?.id);
    const versionIgual = mode === 'version' && aula ? sinCambioReal(aula, values) : false;
    const duplicateMessage = mode === 'create' ? duplicateCreateMessage : duplicateEditMessage;
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting ||
            !sedeLista ||
            !values.sedeId ||
            !normalizarNombre(values.nombre) ||
            !values.tipo ||
            nombreTomado ||
            versionIgual) {
            return;
        }
        if (mode !== 'create' && !aula?.id) {
            setSubmitError('No se encontró el aula.');
            return;
        }
        const payload = {
            sedeId: values.sedeId,
            nombre: normalizarNombre(values.nombre),
            tipo: values.tipo,
            capacidad: values.capacidad === '' ? null : Number(values.capacidad),
        };
        setBusy(true);
        try {
            if (mode === 'create') {
                onCreated?.(await registrarAula(payload));
            }
            else if (mode === 'edit' && aula) {
                onUpdated?.(await actualizarAula(aula.id, { ...payload, activa: values.activa }));
            }
            else if (aula) {
                onVersioned?.(aula.id, await registrarNuevaVersionAula(aula.id, payload));
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : mode === 'version'
                    ? 'No se pudo registrar la nueva versión del aula.'
                    : mode === 'edit'
                        ? 'No se pudo actualizar el aula.'
                        : 'No se pudo registrar el aula.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsxs("div", { className: styles.grid, children: [_jsx(TextField, { id: `${formId}-name`, label: "Nombre del aula", required: true, maxLength: 120, disabled: submitting, placeholder: "Ejemplo: A-101", value: values.nombre, error: nombreTomado ? duplicateMessage : versionIgual ? versionMessage : undefined, onChange: (event) => setValues((current) => ({ ...current, nombre: event.target.value })) }), _jsx(SelectField, { id: `${formId}-type`, label: "Tipo", required: true, disabled: submitting, value: values.tipo, options: tipoOptions, error: attempted && !values.tipo ? 'Seleccione un tipo de aula.' : undefined, onChange: (tipo) => setValues((current) => ({ ...current, tipo: tipo })) }), _jsx(SelectField, { id: `${formId}-campus`, label: "Sede", required: true, disabled: !sedeLista || submitting, value: sedeLista ? values.sedeId : '', options: sedeOptions, error: sedesError ||
                            (attempted && sedeLista && !values.sedeId ? 'Seleccione una sede.' : undefined), onChange: (sedeId) => setValues((current) => ({ ...current, sedeId })) }), _jsx(TextField, { id: `${formId}-capacity`, label: "Capacidad", inputMode: "numeric", disabled: submitting, placeholder: "Opcional", value: values.capacidad, onChange: (event) => setValues((current) => ({
                            ...current,
                            capacidad: event.target.value.replace(/\D/g, ''),
                        })) })] }), mode === 'edit' ? (_jsxs("label", { className: styles.check, htmlFor: `${formId}-active`, children: [_jsx(Checkbox, { id: `${formId}-active`, checked: values.activa, disabled: submitting, onChange: (event) => setValues((current) => ({ ...current, activa: event.target.checked })) }), "El aula est\u00E1 activa"] })) : null, submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: mode === 'version'
                    ? 'La versión anterior queda inactiva y se crea un aula nueva activa. Cambia el nombre, el tipo o la sede.'
                    : mode === 'edit'
                        ? 'Corrige datos, cambia la capacidad o activa e inactiva el aula. Un cambio de identidad del espacio se registra como nueva versión.'
                        : 'El aula queda activa. El nombre no puede repetirse entre las aulas activas de la misma sede.' })] }));
}
export function AulaFormModal({ open, mode, sedes, sedesError = '', aulas, aula, sedeInicial = '', loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, onVersioned, }) {
    const [submitting, setSubmitting] = useState(false);
    const showForm = !loading && !loadError && (mode === 'create' || Boolean(aula));
    const formId = `aula-${mode}-form`;
    const title = mode === 'create' ? 'Nueva aula' : mode === 'version' ? 'Nueva versión del aula' : 'Editar aula';
    const description = mode === 'create'
        ? 'Registra un espacio de la sede seleccionada.'
        : mode === 'version'
            ? 'La versión anterior queda inactiva y se crea un aula nueva.'
            : 'Actualiza el aula seleccionada.';
    const confirmLabel = loadError
        ? undefined
        : loading
            ? 'Cargando...'
            : submitting
                ? 'Guardando...'
                : mode === 'create'
                    ? 'Guardar aula'
                    : mode === 'version'
                        ? 'Registrar versión'
                        : 'Guardar cambios';
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: title, description: description, onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: confirmLabel, confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando aula..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(AulaFormFields, { mode: mode, sedes: sedes, sedesError: sedesError, aulas: aulas, aula: aula, sedeInicial: sedeInicial, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onVersioned: onVersioned, onSubmittingChange: setSubmitting }, mode === 'create' ? 'create' : `${mode}-${aula?.id}`)) : (_jsx("p", { className: styles.status, children: "Cargando aula..." })) }));
}
