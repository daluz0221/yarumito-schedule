import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { StatCard } from '../../molecules/StatCard';
import styles from './ValidationMetrics.module.css';
export function ValidationMetrics({ total, errors, warnings, resolved, pending, }) {
    return (_jsxs("div", { className: styles.row, "aria-label": "Resumen de conflictos", children: [_jsx(StatCard, { value: String(total), label: "Total conflictos", wash: "total" }), _jsx(StatCard, { value: String(errors), label: "Errores", wash: "danger" }), _jsx(StatCard, { value: String(warnings), label: "Advertencias", wash: "warning" }), _jsx(StatCard, { value: String(resolved), label: "Resueltos", wash: "success" }), _jsx(StatCard, { value: String(pending), label: "Pendientes", wash: "neutral" })] }));
}
