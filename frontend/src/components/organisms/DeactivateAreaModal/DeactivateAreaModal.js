import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { ApiError, cambiarEstadoArea } from '../../../api';
import { Modal } from '../../atoms/Modal';
import { Callout } from '../../molecules/Callout';
import { ModalActions } from '../../molecules/ModalActions';
import styles from './DeactivateAreaModal.module.css';
export function DeactivateAreaModal({ open, area, onClose, onUpdated, }) {
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const activating = area?.activa === false;
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
        if (!area?.id) {
            setSubmitError('No se encontró el área a actualizar.');
            return;
        }
        setSubmitError('');
        setSubmitting(true);
        try {
            const updated = await cambiarEstadoArea(area.id, activating);
            onUpdated?.(updated);
            onClose();
        }
        catch (cause) {
            setSubmitError(cause instanceof ApiError
                ? cause.message
                : activating
                    ? 'No se pudo activar el área. Inténtalo de nuevo.'
                    : 'No se pudo inactivar el área. Inténtalo de nuevo.');
        }
        finally {
            setSubmitting(false);
        }
    };
    return (_jsxs(Modal, { open: open, title: activating ? 'Activar área' : 'Inactivar área', description: activating
            ? `¿Deseas activar el área ${area?.nombre ?? ''}?`
            : `¿Deseas inactivar el área ${area?.nombre ?? ''}?`, onClose: handleClose, footer: _jsx(ModalActions, { confirmLabel: submitting
                ? 'Guardando...'
                : activating
                    ? 'Activar área'
                    : 'Inactivar área', disabled: submitting, onCancel: handleClose, onConfirm: handleConfirm }), children: [_jsx(Callout, { children: activating ? (_jsx("p", { children: "El \u00E1rea volver\u00E1 a estar disponible para nuevas configuraciones acad\u00E9micas. Sus asignaturas se conservan." })) : (_jsx("p", { children: "El \u00E1rea dejar\u00E1 de estar disponible para nuevas configuraciones acad\u00E9micas, pero su informaci\u00F3n hist\u00F3rica se conservar\u00E1. Sus asignaturas no se eliminar\u00E1n autom\u00E1ticamente." })) }), submitError ? (_jsx("p", { className: styles.error, role: "alert", children: submitError })) : null] }));
}
