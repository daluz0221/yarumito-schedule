import { jsx as _jsx, Fragment as _Fragment, jsxs as _jsxs } from "react/jsx-runtime";
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './RemoveScheduleClassModal.module.css';
export function RemoveScheduleClassModal({ open, submitting = false, error = '', onClose, onConfirm, }) {
    return (_jsxs(Modal, { open: open, title: "Quitar del horario", onClose: submitting ? () => undefined : onClose, footer: _jsxs(_Fragment, { children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onClose, disabled: submitting, children: "Cancelar" }), _jsx(Button, { variant: "primary", size: "sm", onClick: onConfirm, disabled: submitting, children: submitting ? 'Quitando…' : 'Quitar del horario' })] }), children: [_jsx("p", { className: styles.copy, children: "Esta clase dejar\u00E1 de estar programada en este bloque. La asignaci\u00F3n acad\u00E9mica se conservar\u00E1 y volver\u00E1 a quedar pendiente de programaci\u00F3n." }), error ? (_jsx("p", { className: styles.error, role: "alert", children: error })) : null] }));
}
