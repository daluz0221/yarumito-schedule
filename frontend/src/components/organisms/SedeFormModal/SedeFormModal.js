import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarSede, ApiError, registrarSede } from '../../../api';
import { Checkbox } from '../../atoms/Checkbox';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { TextField } from '../../molecules/TextField';
import styles from './SedeFormModal.module.css';
const emptyValues = {
    nombre: '',
    codigo: '',
    direccion: '',
    esPrincipal: false,
};
function valuesFromSede(sede) {
    if (!sede) {
        return emptyValues;
    }
    return {
        nombre: sede.nombre,
        codigo: sede.codigo,
        direccion: sede.direccion ?? '',
        esPrincipal: sede.esPrincipal,
    };
}
function codigoOcupado(sedes, codigo, sedeId) {
    const normalizado = codigo.trim().toUpperCase();
    if (!normalizado) {
        return false;
    }
    return sedes.some((sede) => sede.id !== sedeId && sede.codigo.trim().toUpperCase() === normalizado);
}
function SedeFormFields({ mode, sedes, sede, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const [values, setValues] = useState(() => mode === 'edit' ? valuesFromSede(sede) : emptyValues);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `sede-${mode}-form`;
    const creating = mode === 'create';
    const codeTaken = codigoOcupado(sedes, values.codigo, sede?.id);
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setSubmitError('');
        if (submitting || !values.nombre.trim() || !values.codigo.trim() || codeTaken) {
            return;
        }
        if (!creating && !sede?.id) {
            setSubmitError('No se encontró la sede a actualizar.');
            return;
        }
        const payload = {
            nombre: values.nombre.trim(),
            codigo: values.codigo.trim(),
            direccion: values.direccion.trim() || null,
            esPrincipal: values.esPrincipal,
        };
        setBusy(true);
        try {
            if (creating) {
                const created = await registrarSede(payload);
                onCreated?.(created);
            }
            else if (sede) {
                const updated = await actualizarSede(sede.id, payload);
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar la sede. Inténtalo de nuevo.'
                    : 'No se pudo actualizar la sede. Inténtalo de nuevo.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsxs("div", { className: styles.grid, children: [_jsx(TextField, { id: `${formId}-name`, label: "Nombre de la sede", name: "nombre", required: true, maxLength: 100, disabled: submitting, placeholder: "Ingresa el nombre de la sede", value: values.nombre, onChange: (event) => setValues((current) => ({ ...current, nombre: event.target.value })) }), _jsx(TextField, { id: `${formId}-code`, label: "C\u00F3digo", name: "codigo", required: true, maxLength: 20, disabled: submitting, placeholder: "Ingresa el c\u00F3digo", value: values.codigo, error: codeTaken ? 'Ya existe una sede con ese código' : undefined, onChange: (event) => setValues((current) => ({
                            ...current,
                            codigo: event.target.value.toUpperCase(),
                        })) })] }), _jsx(TextField, { id: `${formId}-address`, label: "Direcci\u00F3n", name: "direccion", maxLength: 100, disabled: submitting, placeholder: "Ingresa la direcci\u00F3n", value: values.direccion, onChange: (event) => setValues((current) => ({ ...current, direccion: event.target.value })) }), _jsxs("label", { className: styles.check, htmlFor: `${formId}-principal`, children: [_jsx(Checkbox, { id: `${formId}-principal`, name: "esPrincipal", checked: values.esPrincipal, disabled: submitting, onChange: (event) => setValues((current) => ({
                            ...current,
                            esPrincipal: event.target.checked,
                        })) }), "Esta es la sede principal"] }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: "El nombre y el c\u00F3digo se guardan en may\u00FAsculas. Solo puede existir una sede principal." })] }));
}
export function SedeFormModal({ open, mode, sedes, sede, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(sede));
    const formId = `sede-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: creating ? 'Nueva sede' : 'Editar sede', description: creating
            ? 'Registra una sede de la institución.'
            : 'Actualiza la información de la sede seleccionada.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar sede'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando sede..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(SedeFormFields, { mode: mode, sedes: sedes, sede: sede, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, mode === 'edit' ? sede?.id : 'create')) : (_jsx("p", { className: styles.status, children: "Cargando sede..." })) }));
}
