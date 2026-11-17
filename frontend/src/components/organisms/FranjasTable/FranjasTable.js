import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { horaCorta } from '../../../api/turnos';
import { Badge } from '../../atoms/Badge';
import { IconAction } from '../../atoms/IconAction';
import styles from './FranjasTable.module.css';
function etiquetaHoras(value) {
    if (!Number.isFinite(value)) {
        return '—';
    }
    return Number.isInteger(value) ? String(value) : String(value);
}
export function FranjasTable({ rows, emptyMessage, onEdit }) {
    if (rows.length === 0) {
        return _jsx("p", { className: styles.empty, children: emptyMessage });
    }
    return (_jsxs("div", { children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Orden" }), _jsx("th", { children: "N\u00FAmero" }), _jsx("th", { children: "Horario" }), _jsx("th", { children: "Tipo" }), _jsx("th", { children: "Horas" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((franja) => (_jsxs("tr", { children: [_jsx("td", { className: styles.order, children: franja.orden }), _jsx("td", { children: franja.numero }), _jsxs("td", { children: [horaCorta(franja.horaInicio), " \u2013 ", horaCorta(franja.horaFin)] }), _jsx("td", { children: _jsx(Badge, { tone: franja.esDescanso ? 'warning' : 'success', children: franja.esDescanso ? 'Descanso' : 'Académica' }) }), _jsx("td", { children: etiquetaHoras(franja.horasAcademicasEquivalentes) }), _jsx("td", { children: _jsx(IconAction, { label: `Editar franja ${franja.orden}`, onClick: () => onEdit(franja) }) })] }, franja.id))) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((franja) => (_jsxs("li", { className: styles.card, children: [_jsxs("strong", { children: ["Orden ", franja.orden, franja.esDescanso ? ' · Descanso' : ` · Clase ${franja.numero}`] }), _jsxs("p", { children: [horaCorta(franja.horaInicio), " \u2013 ", horaCorta(franja.horaFin)] }), _jsxs("p", { children: [etiquetaHoras(franja.horasAcademicasEquivalentes), " h acad\u00E9micas"] }), _jsx(IconAction, { label: `Editar franja ${franja.orden}`, onClick: () => onEdit(franja) })] }, franja.id))) })] }));
}
