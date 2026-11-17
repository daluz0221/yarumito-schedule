import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { academicYearOptions, academicYears } from '../../../content/academicYear';
import { publicationCopy } from '../../../content/publication';
import { Badge } from '../../atoms/Badge';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { SelectField } from '../../molecules/SelectField';
import styles from './PublicationSummary.module.css';
export function PublicationSummary({ year, view, onYearChange }) {
    return (_jsxs(Card, { as: "section", padding: "lg", className: styles.card, children: [_jsxs("div", { className: styles.top, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: publicationCopy.summaryTitle }), _jsx("div", { className: styles.yearSelect, children: _jsx(SelectField, { id: "publication-year", label: "A\u00F1o lectivo", name: "year", value: year.id, options: academicYearOptions(academicYears), onChange: onYearChange }) })] }), _jsxs("dl", { className: styles.facts, children: [_jsx(Fact, { label: "Horario", value: view.scheduleName }), _jsx(Fact, { label: "A\u00F1o", value: view.yearLabel }), _jsx(Fact, { label: "Sede", value: view.campus }), _jsx(Fact, { label: "Versi\u00F3n", value: String(view.version) }), _jsxs("div", { className: styles.fact, children: [_jsx(Text, { as: "dt", variant: "caption", children: "Estado" }), _jsx("dd", { className: styles.badge, children: _jsx(Badge, { tone: view.status === 'PUBLICADO' ? 'success' : 'warning', children: view.status }) })] }), _jsx(Fact, { label: "Grupos programados", value: String(view.groups) }), _jsx(Fact, { label: "Clases programadas", value: String(view.classes) }), _jsx(Fact, { label: "Errores pendientes", value: String(view.errors), tone: view.errors > 0 ? 'danger' : 'success' }), _jsx(Fact, { label: "Advertencias", value: String(view.warnings), tone: view.warnings > 0 ? 'warning' : 'success' }), _jsx(Fact, { label: "Creado por", value: view.author })] })] }));
}
function Fact({ label, value, tone, }) {
    return (_jsxs("div", { className: styles.fact, children: [_jsx(Text, { as: "dt", variant: "caption", children: label }), _jsx("dd", { className: [styles.value, tone ? styles[tone] : ''].filter(Boolean).join(' '), children: value })] }));
}
