import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { StatCard } from '../../molecules/StatCard';
import styles from './ScheduleMetrics.module.css';
export function ScheduleMetrics({ programmed, assignedCount, placedHours, assignedHours, pendingHours, conflicts, warnings, }) {
    return (_jsxs("div", { className: styles.row, "aria-label": "Resumen del horario", children: [_jsx(StatCard, { value: `${programmed} / ${assignedCount}`, label: "Asignaciones programadas" }), _jsx(StatCard, { value: `${placedHours} / ${assignedHours}`, label: "Horas programadas" }), _jsx(StatCard, { value: `${pendingHours} h`, label: "Pendientes" }), _jsx(StatCard, { value: String(conflicts), label: "Conflictos" }), _jsx(StatCard, { value: String(warnings), label: "Advertencias" })] }));
}
