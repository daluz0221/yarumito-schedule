import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './PublicationMetrics.module.css';
export function PublicationMetrics({ view }) {
    return (_jsxs("div", { className: styles.row, "aria-label": "Indicadores de publicaci\u00F3n", children: [_jsx(Metric, { label: "Estado", value: view.status, tone: view.status === 'PUBLICADO' ? 'success' : 'warning', word: true }), _jsx(Metric, { label: "Errores pendientes", value: String(view.errors), tone: view.errors > 0 ? 'danger' : 'success' }), _jsx(Metric, { label: "Advertencias", value: String(view.warnings), tone: view.warnings > 0 ? 'warning' : 'success' }), _jsx(Metric, { label: "Versi\u00F3n", value: String(view.version) }), _jsx(Metric, { label: "Publicaci\u00F3n", value: view.publicationLabel, tone: view.publicationLabel === 'Publicado' ? 'success' : 'default', word: true })] }));
}
function Metric({ label, value, tone = 'default', word = false, }) {
    return (_jsxs(Card, { as: "article", padding: "sm", className: styles.card, children: [_jsx(Text, { variant: "stat", className: [word ? styles.word : '', tone === 'default' ? '' : styles[tone]].filter(Boolean).join(' '), children: value }), _jsx(Text, { variant: "caption", children: label })] }));
}
