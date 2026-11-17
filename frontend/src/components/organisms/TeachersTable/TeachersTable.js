import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { labelConteoTitulos, labelResumenIdoneidad, } from '../../../content/teacherProfile';
import { formatTeacherName, labelEstado, labelVinculacion, toneEstado, } from '../../../content/teachers';
import { Link } from 'react-router-dom';
import { Card } from '../../atoms/Card';
import { PencilIcon, UserIcon } from '../../atoms/icons';
import { Pagination } from '../../molecules/Pagination';
import { StatusLabel } from '../../molecules/StatusLabel';
import styles from './TeachersTable.module.css';
function TeacherRowActions({ teacher }) {
    const name = formatTeacherName(teacher);
    return (_jsxs("span", { className: styles.rowActions, children: [_jsx(Link, { to: `/dashboard/docentes/${teacher.id}/editar`, className: styles.iconAction, "aria-label": `Editar a ${name}`, title: "Editar", children: _jsx(PencilIcon, {}) }), _jsx(Link, { to: `/dashboard/docentes/${teacher.id}`, className: `${styles.iconAction} ${styles.iconActionProfile}`, "aria-label": `Ver perfil de ${name}`, title: "Ver perfil", children: _jsx(UserIcon, { size: 18 }) })] }));
}
function UnavailableValue() {
    return _jsx("span", { className: styles.pending, children: "No disponible" });
}
function LoadingValue() {
    return _jsx("span", { className: styles.pending, children: "Cargando..." });
}
function TitlesValue({ summary }) {
    if (!summary) {
        return _jsx(LoadingValue, {});
    }
    if (summary.titulos.estado === 'error') {
        return _jsx(UnavailableValue, {});
    }
    return labelConteoTitulos(summary.titulos.cantidad);
}
function SuitabilityValue({ summary }) {
    if (!summary) {
        return _jsx(LoadingValue, {});
    }
    if (summary.idoneidad.estado === 'error') {
        return _jsx(UnavailableValue, {});
    }
    return labelResumenIdoneidad(summary.idoneidad);
}
function AreaValue({ teacher, areas, }) {
    if (areas.estado === 'cargando') {
        return _jsx(LoadingValue, {});
    }
    if (areas.estado === 'error') {
        return _jsx(UnavailableValue, {});
    }
    const nombre = areas.porId[teacher.areaNombramientoId];
    if (!nombre) {
        return teacher.areaNombramientoId ? _jsx(UnavailableValue, {}) : 'Sin área';
    }
    return nombre;
}
export function TeachersTable({ rows, summaries, areas, page, pageSize, total, emptyMessage, onPageChange, onPageSizeChange, }) {
    return (_jsxs(Card, { padding: "sm", className: styles.card, children: [_jsx("div", { className: styles.scroll, children: _jsxs("table", { className: styles.table, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "Documento" }), _jsx("th", { children: "Docente" }), _jsx("th", { children: "\u00C1rea" }), _jsx("th", { children: "Vinculaci\u00F3n" }), _jsx("th", { children: "Contacto" }), _jsx("th", { children: "Estado" }), _jsx("th", { children: "T\u00EDtulos" }), _jsx("th", { children: "Idoneidad" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: rows.map((teacher) => (_jsxs("tr", { children: [_jsxs("td", { children: [teacher.tipoDocumento, " ", teacher.numeroDocumento] }), _jsx("td", { className: styles.name, children: formatTeacherName(teacher) }), _jsx("td", { children: _jsx(AreaValue, { teacher: teacher, areas: areas }) }), _jsx("td", { children: labelVinculacion(teacher.tipoVinculacion) }), _jsx("td", { children: _jsxs("span", { className: styles.contact, children: [_jsx("span", { children: teacher.correoInstitucional }), _jsx("span", { children: teacher.telefono })] }) }), _jsx("td", { children: _jsx(StatusLabel, { label: labelEstado(teacher.estado), tone: toneEstado(teacher.estado) }) }), _jsx("td", { children: _jsx(TitlesValue, { summary: summaries[teacher.id] }) }), _jsx("td", { children: _jsx(SuitabilityValue, { summary: summaries[teacher.id] }) }), _jsx("td", { children: _jsx(TeacherRowActions, { teacher: teacher }) })] }, teacher.id))) })] }) }), _jsx("ul", { className: styles.cards, children: rows.map((teacher) => (_jsxs("li", { className: styles.mobileCard, children: [_jsxs("div", { className: styles.mobileHeader, children: [_jsx("strong", { children: formatTeacherName(teacher) }), _jsx(StatusLabel, { label: labelEstado(teacher.estado), tone: toneEstado(teacher.estado) })] }), _jsxs("p", { children: [teacher.tipoDocumento, " ", teacher.numeroDocumento] }), _jsxs("p", { children: [_jsx(AreaValue, { teacher: teacher, areas: areas }), ' · ', labelVinculacion(teacher.tipoVinculacion)] }), _jsxs("p", { children: [teacher.correoInstitucional, _jsx("br", {}), teacher.telefono] }), _jsxs("p", { children: [_jsx(TitlesValue, { summary: summaries[teacher.id] }), ' · ', _jsx(SuitabilityValue, { summary: summaries[teacher.id] })] }), _jsx(TeacherRowActions, { teacher: teacher })] }, teacher.id))) }), rows.length === 0 ? (_jsx("p", { className: styles.empty, children: emptyMessage ?? 'No se encontraron docentes con esos filtros.' })) : null, _jsx(Pagination, { page: page, pageSize: pageSize, total: total, onPageChange: onPageChange, onPageSizeChange: onPageSizeChange })] }));
}
