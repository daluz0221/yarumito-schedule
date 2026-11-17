import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { actualizarArea, ApiError, listarAreasPredefinidas, registrarArea } from '../../../api';
import { areaFormStatusOptions, areaYesNoOptions, isAreaCodeTaken, } from '../../../content/areas';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './AreaFormModal.module.css';
const emptyValues = {
    nombre: '',
    codigo: '',
    obligatoria: '',
    soloMedia: '',
    activa: '',
};
const duplicateCodeMessage = 'Ya existe un área con este código.';
const requiredChoiceMessage = 'Seleccione una opción.';
function yesNoValue(value) {
    if (value === undefined) {
        return '';
    }
    return value ? 'si' : 'no';
}
function statusValue(activa) {
    if (activa === undefined) {
        return '';
    }
    return activa ? 'activa' : 'inactiva';
}
function valuesFromArea(area) {
    if (!area) {
        return emptyValues;
    }
    return {
        nombre: area.nombre,
        codigo: area.codigo,
        obligatoria: yesNoValue(area.obligatoria),
        soloMedia: yesNoValue(area.soloMedia),
        activa: statusValue(area.activa),
    };
}
function AreaFormFields({ mode, areas, area, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const [values, setValues] = useState(() => mode === 'edit' ? valuesFromArea(area) : emptyValues);
    const [catalogo, setCatalogo] = useState([]);
    const [catalogoCodigo, setCatalogoCodigo] = useState('');
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `area-${mode}-form`;
    const codeTaken = isAreaCodeTaken(areas, values.codigo, area?.id);
    const missingChoice = !values.obligatoria || !values.soloMedia || !values.activa;
    const creating = mode === 'create';
    useEffect(() => {
        const controller = new AbortController();
        listarAreasPredefinidas(controller.signal)
            .then((items) => {
            if (!controller.signal.aborted) {
                setCatalogo(items ?? []);
            }
        })
            .catch(() => {
            if (!controller.signal.aborted) {
                setCatalogo([]);
            }
        });
        return () => controller.abort();
    }, []);
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting) {
            return;
        }
        if (!values.nombre.trim() || !values.codigo.trim() || missingChoice || codeTaken) {
            return;
        }
        if (!creating && !area?.id) {
            setSubmitError('No se encontró el área a actualizar.');
            return;
        }
        const payload = {
            nombre: values.nombre.trim(),
            codigo: values.codigo.trim(),
            obligatoria: values.obligatoria === 'si',
            soloMedia: values.soloMedia === 'si',
            activa: values.activa === 'activa',
        };
        setBusy(true);
        try {
            if (creating) {
                const created = await registrarArea(payload);
                onCreated?.(created);
            }
            else if (area) {
                const updated = await actualizarArea(area.id, payload);
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar el área. Inténtalo de nuevo.'
                    : 'No se pudo actualizar el área. Inténtalo de nuevo.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsx(SelectField, { id: `${formId}-catalog`, label: "\u00C1rea del cat\u00E1logo", name: "catalogo", disabled: submitting || catalogo.length === 0, value: catalogoCodigo, options: [
                    { value: '', label: catalogo.length ? 'Escribir manualmente' : 'Cargando catálogo...' },
                    ...catalogo.map((item) => ({
                        value: item.codigo,
                        label: `${item.nombre} (${item.codigo})`,
                    })),
                ], hint: "El nombre y el c\u00F3digo salen del cat\u00E1logo fijo. Puedes ajustarlos.", onChange: (codigo) => {
                    const opcion = catalogo.find((item) => item.codigo === codigo);
                    setCatalogoCodigo(codigo);
                    if (opcion) {
                        setValues((current) => ({
                            ...current,
                            nombre: opcion.nombre,
                            codigo: opcion.codigo,
                        }));
                    }
                } }), _jsxs("div", { className: styles.grid, children: [_jsx(TextField, { id: `${formId}-name`, label: "Nombre del \u00E1rea", name: "nombre", required: true, disabled: submitting, placeholder: "Ingresa el nombre del \u00E1rea", value: values.nombre, onChange: (event) => setValues((current) => ({ ...current, nombre: event.target.value })) }), _jsx(TextField, { id: `${formId}-code`, label: "C\u00F3digo", name: "codigo", required: true, disabled: submitting, placeholder: "Ingresa el c\u00F3digo", value: values.codigo, error: codeTaken ? duplicateCodeMessage : undefined, onChange: (event) => setValues((current) => ({
                            ...current,
                            codigo: event.target.value.toUpperCase(),
                        })) }), _jsx(SelectField, { id: `${formId}-required`, label: "\u00C1rea obligatoria", name: "obligatoria", value: values.obligatoria, options: areaYesNoOptions, disabled: submitting, error: attempted && !values.obligatoria ? requiredChoiceMessage : undefined, onChange: (obligatoria) => setValues((current) => ({
                            ...current,
                            obligatoria: obligatoria,
                        })) }), _jsx(SelectField, { id: `${formId}-media`, label: "Solo para Media", name: "soloMedia", value: values.soloMedia, options: areaYesNoOptions, disabled: submitting, error: attempted && !values.soloMedia ? requiredChoiceMessage : undefined, onChange: (soloMedia) => setValues((current) => ({
                            ...current,
                            soloMedia: soloMedia,
                        })) })] }), _jsx(SelectField, { id: `${formId}-status`, label: "Estado", name: "activa", value: values.activa, options: areaFormStatusOptions, disabled: submitting, error: attempted && !values.activa ? requiredChoiceMessage : undefined, onChange: (activa) => setValues((current) => ({
                    ...current,
                    activa: activa,
                })) }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: "\u201CSolo para Media\u201D indica si el \u00E1rea se utiliza exclusivamente en los grados de Media." })] }));
}
export function AreaFormModal({ open, mode, areas, area, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(area));
    const formId = `area-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: mode === 'create' ? 'Nueva área académica' : 'Editar área académica', description: mode === 'create'
            ? 'Registra una nueva área para el catálogo académico institucional.'
            : 'Actualiza la información del área seleccionada.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar área'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading, onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando \u00E1rea..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(AreaFormFields, { mode: mode, areas: areas, area: area, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, mode === 'edit' ? area?.id : 'create')) : (_jsx("p", { className: styles.status, children: "Cargando \u00E1rea..." })) }));
}
