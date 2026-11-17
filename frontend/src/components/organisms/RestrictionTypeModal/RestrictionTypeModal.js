import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import styles from './RestrictionTypeModal.module.css';
const choices = [
    { tipo: 'NO_DISPONIBLE', label: 'No disponible' },
    { tipo: 'PREFERENCIA', label: 'Preferencia' },
];
export function RestrictionTypeModal({ open, onSelect, onClose }) {
    return (_jsx(Modal, { open: open, title: "Tipo de restricci\u00F3n", onClose: onClose, children: _jsxs("div", { className: styles.choices, children: [choices.map((choice) => (_jsx(Button, { variant: "muted", size: "sm", onClick: () => onSelect(choice.tipo), children: choice.label }, choice.tipo))), _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cancelar" })] }) }));
}
