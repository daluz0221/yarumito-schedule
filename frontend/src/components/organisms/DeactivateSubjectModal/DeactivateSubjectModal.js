import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { ApiError, cambiarEstadoAsignatura } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
import styles from './DeactivateSubjectModal.module.css';
export function DeactivateSubjectModal({ open, subject, onClose, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const activating = subject?.activa === false;
    const handleClose = () => {
        if (submitting) {
            return;
        }
        onClose();
    };
    const handleConfirm = async () => {
        if (submitting) {
            return;
        }
        if (!subject?.id) {
            setSubmitError('No se encontró la asignatura a actualizar.');
            return;
        }
        setSubmitError('');
        setSubmitting(true);
        try {
            const updated = await cambiarEstadoAsignatura(subject.id, activating);
            onUpdated?.(updated);
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : activating
                    ? 'No se pudo activar la asignatura. Inténtalo de nuevo.'
                    : 'No se pudo inactivar la asignatura. Inténtalo de nuevo.');
        }
        finally {
            setSubmitting(false);
        }
    };
    return (_jsxs(Modal, { open: open, title: activating ? 'Activar asignatura' : 'Inactivar asignatura', description: activating
            ? `¿Deseas activar la asignatura ${subject?.nombre ?? ''}?`
            : `¿Deseas inactivar la asignatura ${subject?.nombre ?? ''}?`, onClose: handleClose, footer: _jsx(ModalActions, { confirmLabel: submitting
                ? 'Guardando...'
                : activating
                    ? 'Activar asignatura'
                    : 'Inactivar asignatura', disabled: submitting, onCancel: handleClose, onConfirm: handleConfirm }), children: [_jsx(Callout, { children: activating ? (_jsx("p", { children: "La asignatura volver\u00E1 a estar disponible para nuevas configuraciones acad\u00E9micas. Sus relaciones hist\u00F3ricas se conservan." })) : (_jsx("p", { children: "La asignatura dejar\u00E1 de estar disponible para nuevas configuraciones acad\u00E9micas. Sus relaciones hist\u00F3ricas con Plan de Estudios, Asignaci\u00F3n Acad\u00E9mica e Idoneidad se conservar\u00E1n." })) }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null] }));
}
