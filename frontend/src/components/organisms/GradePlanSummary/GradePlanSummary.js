import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import styles from './GradePlanSummary.module.css';
export function GradePlanSummary({ grade, configuredHours }) {
    const expected = grade.weeklyHours;
    const difference = Math.abs(expected - configuredHours);
    const pending = Math.max(expected - configuredHours, 0);
    const excess = Math.max(configuredHours - expected, 0);
    const progress = expected === 0 ? 0 : Math.min(100, (configuredHours / expected) * 100);
    const pendingLabel = excess
        ? `Excede ${excess} horas`
        : pending
            ? `Pendiente ${pending} horas`
            : 'Horas completas';
    return (_jsxs(Card, { className: styles.card, children: [_jsx(Text, { as: "h3", variant: "sectionTitle", children: "Resumen del grado" }), _jsxs("dl", { className: styles.facts, children: [_jsx(Fact, { label: "Grado", value: grade.label }), _jsx(Fact, { label: "Tipo", value: grade.level }), _jsx(Fact, { label: "Horas semanales esperadas", value: `${expected} h` }), _jsx(Fact, { label: "Horas configuradas", value: `${configuredHours} h` }), _jsx(Fact, { label: "Diferencia", value: `${difference} h` })] }), _jsxs("div", { className: styles.progressCopy, children: [_jsxs("p", { children: [configuredHours, " de ", expected, " h configuradas"] }), _jsx("p", { className: excess ? styles.warning : pending ? styles.pending : styles.complete, children: pendingLabel })] }), _jsx("div", { className: styles.track, role: "progressbar", "aria-valuemin": 0, "aria-valuemax": expected, "aria-valuenow": Math.min(configuredHours, expected), "aria-label": "Horas configuradas del grado", children: _jsx("span", { className: styles.fill, style: { width: `${progress}%` } }) })] }));
}
function Fact({ label, value }) {
    return (_jsxs("div", { className: styles.fact, children: [_jsx("dt", { children: label }), _jsx("dd", { children: value })] }));
}
