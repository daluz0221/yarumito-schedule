import { jsxs as _jsxs, jsx as _jsx, Fragment as _Fragment } from "react/jsx-runtime";
import { useRef, useState } from 'react';
import { labelAvailability, restrictionApprovalHint, restrictionDayOptions, restrictionEditApprovalHint, restrictionEditTypeHint, restrictionEffect, restrictionTypeHint, restrictionTypeOptions, } from '../../../content/teacherRestrictions';
import { TextArea } from '../../atoms/TextArea';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import { FormField } from '../../molecules/FormField';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import styles from './RestrictionFormModal.module.css';
const requiredChoiceMessage = 'Seleccione una opción.';
const motivoMessage = 'Ingresa el motivo.';
const duplicateMessage = 'Esta franja ya tiene una restricción.';
const fileMessage = 'No se pudo leer el documento soporte.';
const fileSizeMessage = 'El documento soporte no puede superar 1.5 MB.';
const maxFileBytes = 1_500_000;
function createValues(day = '', bandId = '', tipo = '') {
    return { day, bandId, tipo, motivo: '', fileName: '', documentoSoporte: null };
}
function valuesFromRestriction(restriction) {
    return {
        day: restriction.day,
        bandId: restriction.bandId,
        tipo: restriction.status,
        motivo: restriction.motivo,
        fileName: '',
        documentoSoporte: restriction.documentoSoporte,
    };
}
function effectNote(status) {
    const sentence = restrictionEffect(status);
    return `${labelAvailability(status)}: ${sentence.charAt(0).toLowerCase()}${sentence.slice(1)}`;
}
function archivoABase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            const result = typeof reader.result === 'string' ? reader.result : '';
            const comma = result.indexOf(',');
            resolve(comma >= 0 ? result.slice(comma + 1) : result);
        };
        reader.onerror = () => reject(reader.error ?? new Error(fileMessage));
        reader.readAsDataURL(file);
    });
}
export function RestrictionFormModal({ open, mode, year, teacherName, day = '', bandId = '', bandLabel = '', tipo = '', restriction, bandOptions, isTaken, submitting = false, error = '', onClose, onSubmit, }) {
    const fileRef = useRef(null);
    const [values, setValues] = useState(() => mode === 'edit' && restriction
        ? valuesFromRestriction(restriction)
        : createValues(day, bandId, tipo));
    const [attempted, setAttempted] = useState(false);
    const [fileError, setFileError] = useState('');
    const formId = `restriction-${mode}-form`;
    const editing = mode === 'edit';
    const duplicate = !editing &&
        Boolean(values.day && values.bandId && isTaken?.(values.day, values.bandId));
    const attachmentLabel = values.fileName
        ? values.fileName
        : values.documentoSoporte
            ? 'Documento adjunto'
            : 'Ningún archivo adjunto';
    const handleSubmit = (event) => {
        event.preventDefault();
        setAttempted(true);
        if (submitting ||
            !values.day ||
            !values.bandId ||
            !values.tipo ||
            !values.motivo.trim() ||
            duplicate) {
            return;
        }
        onSubmit({
            day: values.day,
            bandId: values.bandId,
            tipo: values.tipo,
            motivo: values.motivo.trim(),
            documentoSoporte: values.documentoSoporte,
        });
    };
    const patch = (partial) => {
        setValues((current) => ({ ...current, ...partial }));
    };
    const attachFile = async (file) => {
        if (!file) {
            return;
        }
        if (file.size > maxFileBytes) {
            setFileError(fileSizeMessage);
            return;
        }
        try {
            const documentoSoporte = await archivoABase64(file);
            setFileError('');
            patch({ fileName: file.name, documentoSoporte });
        }
        catch {
            setFileError(fileMessage);
        }
    };
    return (_jsx(Modal, { open: open, title: editing ? 'Editar restricción' : 'Agregar restricción', description: editing ? undefined : `${teacherName} · Año lectivo ${year}`, onClose: onClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: submitting ? 'Guardando…' : editing ? 'Guardar cambios' : 'Guardar restricción', confirmType: "submit", disabled: submitting, onCancel: onClose }), children: _jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [editing ? (_jsxs("div", { className: styles.context, children: [_jsxs("p", { children: [teacherName, " \u00B7 A\u00F1o lectivo ", year] }), _jsxs("p", { className: styles.slot, children: [values.day, " \u00B7 ", bandLabel] })] })) : null, editing ? null : (_jsxs("div", { className: styles.grid, children: [_jsx(SelectField, { id: `${formId}-day`, label: "D\u00EDa", name: "day", required: true, value: values.day, options: restrictionDayOptions(), error: attempted && !values.day ? requiredChoiceMessage : undefined, onChange: (nextDay) => patch({ day: nextDay }) }), _jsx(SelectField, { id: `${formId}-band`, label: "Franja", name: "band", required: true, value: values.bandId, options: [{ value: '', label: 'Seleccione una franja' }, ...bandOptions], error: attempted && !values.bandId
                                ? requiredChoiceMessage
                                : attempted && duplicate
                                    ? duplicateMessage
                                    : undefined, onChange: (nextBand) => patch({ bandId: nextBand }) })] })), _jsx(SelectField, { id: `${formId}-type`, label: "Tipo", name: "tipo", required: true, value: values.tipo, options: editing
                        ? restrictionTypeOptions.filter((option) => option.value)
                        : restrictionTypeOptions, hint: editing ? restrictionEditTypeHint : restrictionTypeHint, error: attempted && !values.tipo ? requiredChoiceMessage : undefined, onChange: (nextTipo) => patch({ tipo: nextTipo }) }), _jsx(FormField, { id: `${formId}-motivo`, label: "Motivo", required: true, error: attempted && !values.motivo.trim() ? motivoMessage : undefined, children: _jsx(TextArea, { id: `${formId}-motivo`, name: "motivo", required: true, rows: 3, placeholder: "Describe la condici\u00F3n de disponibilidad", value: values.motivo, "aria-invalid": attempted && !values.motivo.trim(), onChange: (event) => patch({ motivo: event.target.value }) }) }), _jsxs("div", { className: styles.file, children: [_jsx(Button, { type: "button", variant: "outline", size: "sm", disabled: submitting, onClick: () => fileRef.current?.click(), children: editing ? 'Vincular documento soporte' : 'Adjuntar archivo' }), _jsx("input", { ref: fileRef, type: "file", className: styles.fileInput, onChange: (event) => {
                                const file = event.target.files?.[0];
                                event.target.value = '';
                                void attachFile(file);
                            } }), _jsxs("p", { children: ["Documento soporte (opcional) \u00B7 ", attachmentLabel] }), fileError ? (_jsx("p", { className: styles.error, role: "alert", children: fileError })) : null] }), _jsx("p", { className: styles.note, children: editing ? restrictionEditApprovalHint : restrictionApprovalHint }), editing ? null : (_jsxs(_Fragment, { children: [_jsx("p", { className: styles.note, children: effectNote('NO_DISPONIBLE') }), _jsx("p", { className: styles.note, children: effectNote('PREFERENCIA') })] })), error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null] }) }));
}
