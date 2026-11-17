import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { assignmentKindDetails, assignmentKindModalHint, assignmentKindModalNote, } from '../../../content/academicAssignment';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import { Text } from '../../atoms/Text';
import styles from './AssignmentKindModal.module.css';
export function AssignmentKindModal({ open, onSelect, onClose, }) {
    return (_jsxs(Modal, { open: open, title: "Tipo de asignaci\u00F3n curricular", onClose: onClose, footer: _jsx("div", { className: styles.actions, children: assignmentKindDetails.map((item) => (_jsx(Button, { variant: "outline", size: "sm", onClick: () => onSelect(item.value), children: item.label.split(' · ')[0] }, item.value))) }), children: [_jsx("div", { className: styles.list, children: assignmentKindDetails.map((item) => (_jsxs("article", { className: styles.item, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: item.label }), _jsx(Text, { variant: "body", children: item.description })] }, item.value))) }), _jsx("p", { className: styles.note, children: assignmentKindModalNote }), _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cerrar" }), _jsx("p", { className: styles.hint, children: assignmentKindModalHint })] }));
}
