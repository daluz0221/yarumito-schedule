import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { etiquetaTipoAula } from '../../../api/aulas';
import { Badge } from '../../atoms/Badge';
import { IconAction, IconActions } from '../../atoms/IconAction';
import { TextLink } from '../../atoms/TextLink';
import styles from './AulasTable.module.css';
function capacidad(aula) {
    return aula.capacidad == null ? '—' : String(aula.capacidad);
}
export function AulasTable({ rows, emptyMessage, onEdit, onVersion, onDelete }) {
    if (rows.length === 0) {
        return _jsx("p", { className: styles.empty, children: emptyMessage });
    }
    return (_jsxs("div", { children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Nombre" }), _jsx("th", { children: "Tipo" }), _jsx("th", { children: "Capacidad" }), _jsx("th", { children: "Estado" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((aula) => (_jsxs("tr", { children: [_jsx("td", { className: styles.name, children: aula.nombre }), _jsx("td", { children: etiquetaTipoAula(aula.tipo) }), _jsx("td", { children: capacidad(aula) }), _jsx("td", { children: aula.activa ? (_jsx(Badge, { tone: "success", children: "Activa" })) : (_jsx(Badge, { tone: "neutral", children: "Inactiva" })) }), _jsx("td", { children: _jsxs("div", { className: styles.actions, children: [_jsxs(IconActions, { children: [_jsx(IconAction, { label: `Editar ${aula.nombre}`, onClick: () => onEdit(aula) }), _jsx(IconAction, { tone: "danger", label: `Eliminar ${aula.nombre}`, onClick: () => onDelete(aula) })] }), _jsx(TextLink, { onClick: () => onVersion(aula), children: "Nueva versi\u00F3n" })] }) })] }, aula.id))) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((aula) => (_jsxs("li", { className: styles.card, children: [_jsx("strong", { children: aula.nombre }), _jsxs("p", { children: [etiquetaTipoAula(aula.tipo), " \u00B7 ", aula.activa ? 'Activa' : 'Inactiva'] }), _jsxs("p", { children: ["Capacidad: ", capacidad(aula)] }), _jsxs("div", { className: styles.actions, children: [_jsxs(IconActions, { children: [_jsx(IconAction, { label: `Editar ${aula.nombre}`, onClick: () => onEdit(aula) }), _jsx(IconAction, { tone: "danger", label: `Eliminar ${aula.nombre}`, onClick: () => onDelete(aula) })] }), _jsx(TextLink, { onClick: () => onVersion(aula), children: "Nueva versi\u00F3n" })] })] }, aula.id))) })] }));
}
