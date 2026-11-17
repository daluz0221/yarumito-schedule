import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { emptyCrearTituloForm, titleLevelOptions, } from '../../../content/teacherProfile';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { FileField } from '../../molecules/FileField';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import { TextField } from '../../molecules/TextField';
import styles from './TitleFormModal.module.css';
export function TitleFormModal({ open, mode, title, loading = false, loadError = '', submitting = false, error = '', onClose, onRetry, onSubmit, }) {
    const [values, setValues] = useState(emptyCrearTituloForm);
    const [localError, setLocalError] = useState('');
    useEffect(() => {
        if (!open) {
            return;
        }
        setLocalError('');
        setValues(mode === 'edit' && title
            ? {
                level: title.level,
                name: title.name,
                institution: title.institution,
                year: title.year,
                supportFile: title.supportFile ?? '',
            }
            : emptyCrearTituloForm);
    }, [mode, open, title]);
    const handleSubmit = (event) => {
        event.preventDefault();
        if (submitting || loading || loadError) {
            return;
        }
        if (!onSubmit) {
            onClose();
            return;
        }
        if (!values.level) {
            setLocalError('El nivel del título es obligatorio');
            return;
        }
        if (!values.name.trim()) {
            setLocalError('El nombre del título es obligatorio');
            return;
        }
        const year = values.year.trim();
        if (year && !/^\d{4}$/.test(year)) {
            setLocalError('El año de graduación debe tener 4 dígitos');
            return;
        }
        if (year) {
            const parsedYear = Number(year);
            const maxYear = new Date().getFullYear() + 1;
            if (parsedYear < 1900 || parsedYear > maxYear) {
                setLocalError(`El año de graduación debe estar entre 1900 y ${maxYear}`);
                return;
            }
        }
        setLocalError('');
        onSubmit(values);
    };
    return (_jsx(Modal, { open: open, title: mode === 'add' ? 'Agregar título profesional' : 'Editar título profesional', description: mode === 'add'
            ? 'Registra la formación académica que respalda el perfil profesional del docente.'
            : 'Corrige los datos del título profesional registrado para este docente.', onClose: onClose, footer: _jsx(ModalActions, { formId: "title-form", confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : mode === 'add'
                            ? 'Guardar título'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading, onCancel: onClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando t\u00EDtulo profesional..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : (_jsxs("form", { id: "title-form", className: styles.form, noValidate: true, onSubmit: handleSubmit, children: [_jsx(SelectField, { id: "title-level", label: "Nivel", required: true, value: values.level, options: titleLevelOptions, hint: "Opciones: Normalista, Licenciatura, Profesional, Especializaci\u00F3n, Maestr\u00EDa y Doctorado.", onChange: (level) => setValues((current) => ({ ...current, level })) }), _jsx(TextField, { id: "title-name", label: "Nombre del t\u00EDtulo", required: true, placeholder: "Ej. Licenciatura en Matem\u00E1ticas", value: values.name, onChange: (event) => setValues((current) => ({ ...current, name: event.target.value })) }), _jsx(TextField, { id: "title-institution", label: "Instituci\u00F3n", placeholder: "Ej. Universidad de Antioquia", value: values.institution, onChange: (event) => setValues((current) => ({
                        ...current,
                        institution: event.target.value,
                    })) }), _jsx(TextField, { id: "title-year", label: "A\u00F1o de graduaci\u00F3n", placeholder: "Ej. 2018", inputMode: "numeric", value: values.year, onChange: (event) => setValues((current) => ({ ...current, year: event.target.value })) }), _jsx(FileField, { id: "title-support", label: "Soporte acad\u00E9mico", fileName: values.supportFile, hint: "Adjunta el documento que respalda el t\u00EDtulo profesional, si se encuentra disponible.", onFileChange: (supportFile) => setValues((current) => ({ ...current, supportFile })) }), error || localError ? (_jsx("p", { className: styles.error, role: "alert", children: error || localError })) : null] })) }));
}
