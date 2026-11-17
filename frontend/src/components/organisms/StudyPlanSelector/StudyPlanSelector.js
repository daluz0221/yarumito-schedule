import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { labelYearStatus, } from '../../../content/academicYear';
import { Card } from '../../atoms/Card';
import { FormField } from '../../molecules/FormField';
import { SelectField } from '../../molecules/SelectField';
import styles from './StudyPlanSelector.module.css';
export function StudyPlanSelector({ yearId, yearOptions, yearsSummary, yearDisabled = false, gradeId, gradeOptions, gradeError, gradeDisabled = false, status, onYearChange, onGradeChange, }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsxs("form", { className: styles.filters, onSubmit: (event) => event.preventDefault(), children: [_jsx(SelectField, { id: "plan-year", label: "A\u00F1o lectivo", name: "year", value: yearDisabled ? '' : yearId, options: yearOptions, disabled: yearDisabled, onChange: onYearChange }), _jsx(SelectField, { id: "plan-grade", label: "Grado", name: "grade", value: gradeDisabled ? '' : gradeId, options: gradeOptions, disabled: gradeDisabled, error: gradeError || undefined, onChange: onGradeChange }), _jsx(FormField, { id: "plan-status", label: "Estado", children: _jsx("div", { className: styles.status, id: "plan-status", children: _jsx("span", { className: status === 'ACTIVO' ? styles.active : styles.muted, children: status ? labelYearStatus(status) : '—' }) }) })] }), _jsxs("p", { className: styles.hint, children: ["A\u00F1os disponibles: ", yearsSummary] })] }));
}
