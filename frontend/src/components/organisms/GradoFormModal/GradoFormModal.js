import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { actualizarGrado, ApiError, registrarGrado } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { TextLink } from '../../atoms/TextLink';
import { ModalActions } from '../../molecules/ModalActions';
import { SelectField } from '../../molecules/SelectField';
import styles from './GradoFormModal.module.css';
const NIVELES = [
    { nivel: 6, nombre: 'Sexto', esMedia: false, prioridad: 5, horas: 30 },
    { nivel: 7, nombre: 'Séptimo', esMedia: false, prioridad: 4, horas: 30 },
    { nivel: 8, nombre: 'Octavo', esMedia: false, prioridad: 3, horas: 30 },
    { nivel: 9, nombre: 'Noveno', esMedia: false, prioridad: 2, horas: 30 },
    { nivel: 10, nombre: 'Décimo', esMedia: true, prioridad: 1, horas: 37 },
    { nivel: 11, nombre: 'Undécimo', esMedia: true, prioridad: 1, horas: 37 },
];
const NIVEL_OPTIONS = [
    { value: '', label: 'Seleccione un grado' },
    ...NIVELES.map((item) => ({
        value: String(item.nivel),
        label: `${item.nivel}° ${item.nombre}`,
    })),
];
function nivelOcupado(grados, nivel, gradoId) {
    return grados.some((grado) => grado.id !== gradoId && grado.nivel === nivel);
}
function GradoFormFields({ mode, grados, grado, onClose, onCreated, onUpdated, onSubmittingChange, }) {
    const [nivel, setNivel] = useState(mode === 'edit' && grado ? String(grado.nivel) : '');
    const [attempted, setAttempted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const formId = `grado-${mode}-form`;
    const creating = mode === 'create';
    const nivelNumero = Number(nivel);
    const perfil = NIVELES.find((item) => item.nivel === nivelNumero);
    const ocupado = perfil ? nivelOcupado(grados, perfil.nivel, grado?.id) : false;
    const setBusy = (next) => {
        setSubmitting(next);
        onSubmittingChange(next);
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setAttempted(true);
        setSubmitError('');
        if (submitting || !perfil || ocupado) {
            return;
        }
        if (!creating && !grado?.id) {
            setSubmitError('No se encontró el grado a actualizar.');
            return;
        }
        setBusy(true);
        try {
            if (creating) {
                const created = await registrarGrado({ nivel: perfil.nivel });
                onCreated?.(created);
            }
            else if (grado) {
                const updated = await actualizarGrado(grado.id, { nivel: perfil.nivel });
                onUpdated?.(updated);
            }
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : creating
                    ? 'No se pudo registrar el grado. Inténtalo de nuevo.'
                    : 'No se pudo actualizar el grado. Inténtalo de nuevo.');
        }
        finally {
            setBusy(false);
        }
    };
    return (_jsxs("form", { id: formId, className: styles.form, onSubmit: handleSubmit, children: [_jsx(SelectField, { id: `${formId}-nivel`, label: "Grado", name: "nivel", required: true, value: nivel, options: NIVEL_OPTIONS, disabled: submitting, error: attempted && !perfil
                    ? 'Seleccione un grado.'
                    : ocupado
                        ? 'Ya existe un grado de ese nivel'
                        : undefined, onChange: setNivel }), perfil ? (_jsxs("dl", { className: styles.preview, children: [_jsxs("div", { children: [_jsx("dt", { children: "Nombre" }), _jsx("dd", { children: perfil.nombre })] }), _jsxs("div", { children: [_jsx("dt", { children: "Tipo" }), _jsx("dd", { children: perfil.esMedia ? 'Media' : 'Básica secundaria' })] }), _jsxs("div", { children: [_jsx("dt", { children: "Prioridad de asignaci\u00F3n" }), _jsx("dd", { children: perfil.prioridad })] }), _jsxs("div", { children: [_jsx("dt", { children: "Horas semanales" }), _jsx("dd", { children: perfil.horas })] })] })) : null, submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null, _jsx("p", { className: styles.hint, children: "El nombre, si es media, la prioridad y las horas semanales los calcula el servidor a partir del nivel." })] }));
}
export function GradoFormModal({ open, mode, grados, grado, loading = false, loadError = '', onRetry, onClose, onCreated, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const creating = mode === 'create';
    const showForm = !loading && !loadError && (creating || Boolean(grado));
    const formId = `grado-${mode}-form`;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    return (_jsx(Modal, { open: open, title: creating ? 'Nuevo grado' : 'Editar grado', description: creating
            ? 'Registra un grado de 6° a 11°. El resto de datos se calcula solo.'
            : 'Cambia el nivel del grado. El servidor recalcula nombre, tipo, prioridad y horas.', onClose: handleClose, footer: _jsx(ModalActions, { formId: formId, confirmLabel: loadError
                ? undefined
                : loading
                    ? 'Cargando...'
                    : submitting
                        ? 'Guardando...'
                        : creating
                            ? 'Guardar grado'
                            : 'Guardar cambios', confirmType: "submit", disabled: submitting || loading || Boolean(loadError), onCancel: handleClose }), children: loading ? (_jsx("p", { className: styles.status, children: "Cargando grado..." })) : loadError ? (_jsxs("div", { className: styles.feedback, children: [_jsx("p", { className: styles.error, role: "alert", children: loadError }), onRetry ? _jsx(TextLink, { onClick: onRetry, children: "Reintentar" }) : null] })) : showForm ? (_jsx(GradoFormFields, { mode: mode, grados: grados, grado: grado, onClose: onClose, onCreated: onCreated, onUpdated: onUpdated, onSubmittingChange: setSubmitting }, mode === 'edit' ? grado?.id : 'create')) : (_jsx("p", { className: styles.status, children: "Cargando grado..." })) }));
}
