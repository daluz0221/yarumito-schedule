import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './PlanSubjectPickerModal.module.css';
export function PlanSubjectPickerModal({ open, year, groupCode, gradeLabel, subjects, onSelect, onClose, }) {
    return (_jsx(Modal, { open: open, title: "Asignaturas del plan de estudios", description: `${year} · Grupo ${groupCode} · Grado ${gradeLabel}`, onClose: onClose, footer: _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cancelar" }), children: _jsx("div", { className: styles.choices, children: subjects.map((subject) => (_jsxs(Button, { variant: "outline", size: "sm", onClick: () => onSelect(subject), children: [subject.subject, " \u00B7 ", subject.requiredHours, " h requeridas"] }, subject.key))) }) }));
}
