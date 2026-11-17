import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { labelYearStatus, } from '../../../content/academicYear';
import { FormField } from '../../molecules/FormField';
import { Card } from '../../atoms/Card';
import { RestrictionTeacherModal } from '../RestrictionTeacherModal';
import { RestrictionYearModal } from '../RestrictionYearModal';
import styles from './RestrictionSelector.module.css';
export function RestrictionSelector({ status, year, years, teacherName, teachers, onYearChange, onTeacherChange, }) {
    const [picker, setPicker] = useState(null);
    const hint = status === 'ACTIVO'
        ? `${year} — Activo · Administración de restricciones habilitada`
        : status === 'CERRADO'
            ? `${year} — Cerrado · Solo lectura`
            : `${year} — ${labelYearStatus(status)}`;
    return (_jsxs(Card, { className: styles.card, children: [_jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(FormField, { id: "restriction-year", label: "A\u00F1o lectivo", children: _jsx("button", { type: "button", id: "restriction-year", className: styles.trigger, onClick: () => setPicker('year'), children: year }) }), _jsx(FormField, { id: "restriction-teacher", label: "Docente", children: _jsx("button", { type: "button", id: "restriction-teacher", className: styles.trigger, disabled: teachers.length === 0, onClick: () => setPicker('teacher'), children: teacherName || 'Sin docentes' }) })] }), _jsx("p", { className: styles.hint, children: hint }), picker === 'year' ? (_jsx(RestrictionYearModal, { open: true, years: years, onSelect: (nextYearId) => {
                    onYearChange(nextYearId);
                    setPicker(null);
                }, onClose: () => setPicker(null) })) : null, picker === 'teacher' ? (_jsx(RestrictionTeacherModal, { open: true, teachers: teachers, onSelect: (nextTeacherId) => {
                    onTeacherChange(nextTeacherId);
                    setPicker(null);
                }, onClose: () => setPicker(null) })) : null] }));
}
