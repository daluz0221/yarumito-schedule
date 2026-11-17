import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Modal } from '../../atoms/Modal';
import { ModalActions } from '../../molecules/ModalActions';
import styles from './RemoveAssignmentModal.module.css';
export function RemoveAssignmentModal({ open, variant = 'curricular', year, groupCode, subjectName, teacherName, hours, onClose, onConfirm, }) {
    const activity = variant === 'activity';
    return (_jsx(Modal, { open: open, title: activity ? 'Quitar asignación de actividad' : 'Quitar asignación', description: activity
            ? `${teacherName} · ${subjectName} · ${hours} h`
            : `${year} · Grupo ${groupCode} · ${subjectName} · ${teacherName} · ${hours} h`, onClose: onClose, footer: _jsx(ModalActions, { confirmLabel: "Quitar asignaci\u00F3n", onCancel: onClose, onConfirm: onConfirm }), children: _jsxs("div", { className: styles.copy, children: [_jsx("p", { children: activity
                        ? 'Esta asignación dejará de formar parte de la carga de actividades del docente.'
                        : 'Esta asignación dejará de formar parte de la carga académica del grupo.' }), activity ? (_jsx("p", { children: "Se conserva el docente y la actividad institucional." })) : null] }) }));
}
