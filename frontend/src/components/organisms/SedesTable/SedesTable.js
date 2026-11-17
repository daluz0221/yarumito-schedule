import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Badge } from '../../atoms/Badge';
import { IconAction } from '../../atoms/IconAction';
import styles from './SedesTable.module.css';
export function SedesTable({ rows, emptyMessage, onEdit }) {
    if (rows.length === 0) {
        return _jsx("p", { className: styles.empty, children: emptyMessage });
    }
    return (_jsxs("div", { children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "C\u00F3digo" }), _jsx("th", { children: "Sede" }), _jsx("th", { children: "Direcci\u00F3n" }), _jsx("th", { children: "Principal" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((sede) => (_jsxs("tr", { children: [_jsx("td", { className: styles.code, children: sede.codigo }), _jsx("td", { children: sede.nombre }), _jsx("td", { children: sede.direccion?.trim() || '—' }), _jsx("td", { children: sede.esPrincipal ? (_jsx(Badge, { tone: "success", children: "Principal" })) : ('—') }), _jsx("td", { children: _jsx(IconAction, { label: `Editar ${sede.nombre}`, onClick: () => onEdit(sede) }) })] }, sede.id))) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((sede) => (_jsxs("li", { className: styles.card, children: [_jsx("strong", { children: sede.nombre }), _jsxs("p", { children: [sede.codigo, sede.esPrincipal ? ' · Principal' : ''] }), _jsx("p", { children: sede.direccion?.trim() || 'Sin dirección' }), _jsx(IconAction, { label: `Editar ${sede.nombre}`, onClick: () => onEdit(sede) })] }, sede.id))) })] }));
}
