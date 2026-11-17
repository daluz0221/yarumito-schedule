import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { scheduleNameForYear } from '../../../content/validations';
import { Badge } from '../../atoms/Badge';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { SelectField } from '../../molecules/SelectField';
import styles from './ValidationScheduleCard.module.css';
export function ValidationScheduleCard({ year, yearOptions, yearDisabled = false, versionLabel, scheduleName, campusLabel, statusLabel, statusTone, onYearChange, }) {
    return (_jsxs(Card, { as: "section", padding: "lg", className: styles.card, children: [_jsxs("div", { className: styles.top, children: [_jsxs("div", { className: styles.copy, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: "Horario en validaci\u00F3n" }), _jsx(Text, { variant: "body", children: "Se consultan los conflictos guardados del horario de este a\u00F1o y esta sede. Elige el grupo para abrir su horario." })] }), _jsx("div", { className: styles.yearSelect, children: _jsx(SelectField, { id: "validation-year", label: "A\u00F1o lectivo", name: "year", value: year?.id ?? '', options: yearOptions, disabled: yearDisabled, onChange: onYearChange }) })] }), _jsxs("dl", { className: styles.facts, children: [_jsx(Fact, { label: "A\u00F1o lectivo", value: year ? String(year.year) : '—' }), _jsx(Fact, { label: "Sede", value: campusLabel.replace(/^Sede\s+/i, '') || '—' }), _jsx(Fact, { label: "Horario", value: scheduleName || (year ? scheduleNameForYear(year) : '—') }), _jsx(Fact, { label: "Versi\u00F3n", value: versionLabel }), _jsxs("div", { className: styles.fact, children: [_jsx(Text, { as: "dt", variant: "caption", children: "Estado" }), _jsx("dd", { className: styles.badge, children: _jsx(Badge, { tone: statusTone, children: statusLabel }) })] })] })] }));
}
function Fact({ label, value }) {
    return (_jsxs("div", { className: styles.fact, children: [_jsx(Text, { as: "dt", variant: "caption", children: label }), _jsx("dd", { className: styles.value, children: value })] }));
}
