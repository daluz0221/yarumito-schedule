import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { Button } from '../../atoms/Button';
import { Modal } from '../../atoms/Modal';
import { TextInput } from '../../atoms/TextInput';
import { FormField } from '../../molecules/FormField';
import styles from './RestrictionTeacherModal.module.css';
function normalize(value) {
    return value
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}
export function RestrictionTeacherModal({ open, teachers, onSelect, onClose, }) {
    const [query, setQuery] = useState('');
    const needle = normalize(query);
    const visible = teachers.filter((teacher) => {
        if (!teacher.active) {
            return false;
        }
        if (!needle) {
            return true;
        }
        return normalize(teacher.name).includes(needle);
    });
    return (_jsx(Modal, { open: open, title: "Seleccionar docente", onClose: onClose, children: _jsxs("div", { className: styles.body, children: [_jsx(FormField, { id: "restriction-teacher-search", label: "Buscar docente", children: _jsx(TextInput, { id: "restriction-teacher-search", name: "teacherQuery", inputSize: "sm", placeholder: "Buscar por nombre", value: query, onChange: (event) => setQuery(event.target.value) }) }), _jsx("p", { className: styles.caption, children: "Docentes activos \u00B7 Sede Principal" }), _jsxs("div", { className: styles.choices, children: [visible.length === 0 ? (_jsx("p", { className: styles.empty, children: "No se encontraron docentes." })) : (visible.map((teacher) => (_jsxs(Button, { variant: "muted", size: "sm", onClick: () => onSelect(teacher.id), children: [teacher.name, " \u00B7 ", teacher.areaName] }, teacher.id)))), _jsx(Button, { variant: "muted", size: "sm", onClick: onClose, children: "Cancelar" })] })] }) }));
}
