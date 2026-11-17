import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { labelAplicacion, labelClasificacion, labelEstadoArea, } from '../../../content/areas';
import { PENDING_BACKEND_LABEL } from '../../../content/teachers';
import { Badge } from '../../atoms/Badge';
import { BanIcon, CheckIcon, PencilIcon } from '../../atoms/icons';
import styles from './AcademicAreasTable.module.css';
export function AcademicAreasTable({ rows, selectedId, emptyMessage, onSelect, onEdit, onDeactivate, }) {
    return (_jsxs("div", { children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "C\u00F3digo" }), _jsx("th", { children: "\u00C1rea" }), _jsx("th", { children: "Clasificaci\u00F3n" }), _jsx("th", { children: "Aplicaci\u00F3n" }), _jsx("th", { children: "Estado" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((area) => {
                                const selected = area.id === selectedId;
                                return (_jsxs("tr", { className: selected ? styles.selected : styles.row, tabIndex: 0, "aria-selected": selected, onClick: (event) => {
                                        if (event.target instanceof Element && event.target.closest('button')) {
                                            return;
                                        }
                                        onSelect(area.id);
                                    }, onKeyDown: (event) => selectOnKey(event, () => onSelect(area.id)), children: [_jsx("td", { className: styles.code, children: area.codigo }), _jsx("td", { children: _jsx(AreaName, { nombre: area.nombre, selected: selected }) }), _jsx("td", { children: _jsx(AreaValue, { label: labelClasificacion(area.obligatoria), tone: area.obligatoria ? 'success' : 'muted' }) }), _jsx("td", { children: _jsx(AreaValue, { label: labelAplicacion(area.soloMedia), tone: area.soloMedia ? 'amber' : 'info' }) }), _jsx("td", { children: _jsx(AreaValue, { label: labelEstadoArea(area.activa), tone: area.activa === true ? 'success' : 'muted' }) }), _jsx("td", { children: _jsx(AreaActions, { nombre: area.nombre, activa: area.activa, onEdit: () => onEdit(area), onDeactivate: () => onDeactivate(area) }) })] }, area.id));
                            }) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((area) => {
                    const selected = area.id === selectedId;
                    return (_jsxs("li", { className: selected ? styles.mobileCardSelected : styles.mobileCard, children: [_jsxs("button", { type: "button", className: styles.mobileSelect, "aria-pressed": selected, onClick: () => onSelect(area.id), children: [_jsxs("div", { className: styles.mobileHeader, children: [_jsx(AreaName, { nombre: area.nombre, selected: selected }), _jsx(AreaValue, { label: labelEstadoArea(area.activa), tone: area.activa === true ? 'success' : 'muted' })] }), _jsx("p", { className: styles.code, children: area.codigo }), _jsxs("p", { className: styles.meta, children: [_jsx(AreaValue, { label: labelClasificacion(area.obligatoria), tone: area.obligatoria ? 'success' : 'muted' }), _jsx(AreaValue, { label: labelAplicacion(area.soloMedia), tone: area.soloMedia ? 'amber' : 'info' })] })] }), _jsx(AreaActions, { nombre: area.nombre, activa: area.activa, onEdit: () => onEdit(area), onDeactivate: () => onDeactivate(area) })] }));
                }) }), rows.length === 0 ? _jsx("p", { className: styles.empty, children: emptyMessage }) : null] }));
}
function AreaName({ nombre, selected }) {
    return (_jsxs("span", { className: styles.name, children: [_jsx("strong", { children: nombre }), selected ? _jsx("span", { className: styles.selectedLabel, children: "Seleccionada" }) : null] }));
}
function AreaActions({ nombre, activa, onEdit, onDeactivate, }) {
    const inactive = activa === false;
    const toggleLabel = inactive ? 'Activar' : 'Inactivar';
    return (_jsxs("span", { className: styles.actions, children: [_jsx("button", { type: "button", className: styles.iconAction, onClick: onEdit, "aria-label": `Editar ${nombre}`, title: "Editar", children: _jsx(PencilIcon, {}) }), _jsx("button", { type: "button", className: `${styles.iconAction} ${inactive ? styles.iconActionOn : styles.iconActionOff}`, onClick: onDeactivate, "aria-label": `${toggleLabel} ${nombre}`, title: toggleLabel, children: inactive ? _jsx(CheckIcon, {}) : _jsx(BanIcon, {}) })] }));
}
function AreaValue({ label, tone }) {
    if (!label) {
        return _jsx("span", { className: styles.pending, children: PENDING_BACKEND_LABEL });
    }
    return _jsx(Badge, { tone: tone, children: label });
}
function selectOnKey(event, select) {
    if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        select();
    }
}
