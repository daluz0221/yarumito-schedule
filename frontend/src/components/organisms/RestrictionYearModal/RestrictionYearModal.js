import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { labelYearStatus } from '../../../content/academicYear';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './RestrictionYearModal.module.css';
function yearChoiceLabel(year) {
    if (year.status === 'CERRADO') {
        return `${year.year} — Cerrado · Solo lectura`;
    }
    return `${year.year} — ${labelYearStatus(year.status)}`;
}
function orderedYears(years) {
    const current = years.find((year) => year.current);
    const rest = years.filter((year) => year.id !== current?.id);
    return current ? [current, ...rest] : years;
}
export function RestrictionYearModal({ open, years, onSelect, onClose }) {
    const choices = orderedYears(years);
    return (_jsx(Modal, { open: open, title: "Seleccionar a\u00F1o lectivo", onClose: onClose, children: _jsxs("div", { className: styles.choices, children: [choices.length === 0 ? (_jsx("p", { className: styles.empty, children: "No hay a\u00F1os lectivos registrados." })) : (choices.map((year) => (_jsx(Button, { variant: "muted", size: "sm", onClick: () => onSelect(year.id), children: yearChoiceLabel(year) }, year.id)))), _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cancelar" })] }) }));
}
