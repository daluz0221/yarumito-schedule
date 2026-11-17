import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { etiquetaGrado } from '../../../api/grados';
import { Badge } from '../../atoms/Badge';
import { IconAction } from '../../atoms/IconAction';
import styles from './GradosTable.module.css';
export function GradosTable({ rows, emptyMessage, onEdit }) {
    if (rows.length === 0) {
        return _jsx("p", { className: styles.empty, children: emptyMessage });
    }
    return (_jsxs("div", { children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Nivel" }), _jsx("th", { children: "Grado" }), _jsx("th", { children: "Tipo" }), _jsx("th", { children: "Prioridad" }), _jsx("th", { children: "Horas" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((grado) => (_jsxs("tr", { children: [_jsxs("td", { className: styles.nivel, children: [grado.nivel, "\u00B0"] }), _jsx("td", { children: etiquetaGrado(grado.nombre) }), _jsx("td", { children: grado.esMedia ? _jsx(Badge, { tone: "success", children: "Media" }) : 'Básica' }), _jsx("td", { children: grado.prioridadAsignacion }), _jsx("td", { children: grado.horasSemanalesEsperadas }), _jsx("td", { children: _jsx(IconAction, { label: `Editar ${etiquetaGrado(grado.nombre)}`, onClick: () => onEdit(grado) }) })] }, grado.id))) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((grado) => (_jsxs("li", { className: styles.card, children: [_jsxs("strong", { children: [grado.nivel, "\u00B0 ", etiquetaGrado(grado.nombre)] }), _jsxs("p", { children: [grado.esMedia ? 'Media' : 'Básica', " \u00B7 Prioridad ", grado.prioridadAsignacion, " \u00B7", ' ', grado.horasSemanalesEsperadas, " h"] }), _jsx(IconAction, { label: `Editar ${etiquetaGrado(grado.nombre)}`, onClick: () => onEdit(grado) })] }, grado.id))) })] }));
}
