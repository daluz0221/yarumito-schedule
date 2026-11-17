import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { validationReadyCopy } from '../../../content/validations';
import { Badge } from '../../atoms/Badge';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './ValidationReadyModal.module.css';
export function ValidationReadyModal({ open, hasWarnings, onClose, onOpenSchedule, onPreparePublish, }) {
    return (_jsx(Modal, { open: open, title: validationReadyCopy.title, onClose: onClose, footer: _jsxs("div", { className: styles.actions, children: [_jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cerrar" }), _jsx(Button, { variant: "outline", size: "sm", onClick: onOpenSchedule, children: "Volver al horario" }), _jsx(Button, { variant: "primary", size: "sm", onClick: onPreparePublish, children: "Preparar publicaci\u00F3n" })] }), children: _jsxs("div", { className: styles.body, children: [_jsx("div", { className: styles.pills, children: _jsx(Badge, { tone: "success", children: validationReadyCopy.badge }) }), _jsx("p", { className: styles.message, children: validationReadyCopy.message }), hasWarnings ? _jsx("p", { className: styles.note, children: validationReadyCopy.warnings }) : null] }) }));
}
