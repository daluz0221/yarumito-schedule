import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { horaCorta } from '../../../api/turnos';
import { IconAction } from '../../atoms/IconAction';
import styles from './TurnosTable.module.css';
export function TurnosTable({ rows, emptyMessage, onEdit }) {
    if (rows.length === 0) {
        return _jsx("p", { className: styles.empty, children: emptyMessage });
    }
    return (_jsxs("div", { children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Turno" }), _jsx("th", { children: "Horario" }), _jsx("th", { children: "Clase" }), _jsx("th", { children: "Clases / d\u00EDa" }), _jsx("th", { children: "Antes del descanso" }), _jsx("th", { children: "Descanso" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((turno) => (_jsxs("tr", { children: [_jsx("td", { className: styles.name, children: turno.nombre }), _jsxs("td", { children: [horaCorta(turno.horaInicio), " \u2013 ", horaCorta(turno.horaFin)] }), _jsxs("td", { children: [turno.duracionClaseMinutos, " min"] }), _jsx("td", { children: turno.clasesPorDia }), _jsx("td", { children: turno.clasesAntesDeDescanso }), _jsxs("td", { children: [turno.duracionDescansoMinutos, " min"] }), _jsx("td", { children: _jsx(IconAction, { label: `Editar ${turno.nombre}`, onClick: () => onEdit(turno) }) })] }, turno.id))) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((turno) => (_jsxs("li", { className: styles.card, children: [_jsx("strong", { children: turno.nombre }), _jsxs("p", { children: [horaCorta(turno.horaInicio), " \u2013 ", horaCorta(turno.horaFin)] }), _jsxs("p", { children: [turno.clasesPorDia, " clases de ", turno.duracionClaseMinutos, " min \u00B7 descanso", ' ', turno.duracionDescansoMinutos, " min"] }), _jsx(IconAction, { label: `Editar ${turno.nombre}`, onClick: () => onEdit(turno) })] }, turno.id))) })] }));
}
