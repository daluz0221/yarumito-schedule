import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { formatVigencia, isIdoneidadVigente, labelTipoIdoneidad, resolveTituloSoporte, } from '../../../content/teacherProfile';
import { Button } from '../../atoms/Button';
import { Card } from '../../atoms/Card';
import { IconAction } from '../../atoms/IconAction';
import { PlusIcon } from '../../atoms/icons';
import { Table } from '../../atoms/Table';
import { Text } from '../../atoms/Text';
import { TextLink } from '../../atoms/TextLink';
import { RowActions } from '../../molecules/RowActions';
import { SectionHeader } from '../../molecules/SectionHeader';
import { ValidityStatus } from '../../molecules/ValidityStatus';
import styles from './TeacherSuitabilitySection.module.css';
export function TeacherSuitabilitySection({ items, titles, titlesUnavailable = false, areaNameById, subjectNameById, onAdd, onEdit, onEnd, }) {
    const titleLabel = (tituloId) => {
        if (titlesUnavailable && tituloId) {
            return 'No se pudieron cargar los títulos';
        }
        return resolveTituloSoporte(tituloId, titles);
    };
    const subjectLabel = (asignaturaId) => {
        if (!asignaturaId) {
            return 'Toda el área';
        }
        return subjectNameById(asignaturaId) || 'Cargando...';
    };
    return (_jsxs("section", { className: styles.section, children: [_jsx(SectionHeader, { title: "Idoneidad acad\u00E9mica", description: "Define las \u00E1reas y asignaturas para las que el docente se encuentra habilitado de acuerdo con su perfil profesional.", action: _jsxs(Button, { size: "sm", onClick: onAdd, children: [_jsx(PlusIcon, {}), "Agregar idoneidad"] }) }), _jsxs(Card, { padding: "sm", className: styles.card, children: [_jsxs(Table, { minWidth: 880, className: styles.desktopOnly, children: [_jsx("thead", { children: _jsxs("tr", { children: [_jsx("th", { children: "\u00C1rea" }), _jsx("th", { children: "Asignatura" }), _jsx("th", { children: "Tipo" }), _jsx("th", { children: "T\u00EDtulo soporte" }), _jsx("th", { children: "Vigencia" }), _jsx("th", { children: "Acciones" })] }) }), _jsx("tbody", { children: items.map((item) => (_jsxs("tr", { children: [_jsx("td", { children: areaNameById(item.areaId) }), _jsx("td", { className: item.asignaturaId ? styles.strong : styles.pending, children: subjectLabel(item.asignaturaId) }), _jsx("td", { children: labelTipoIdoneidad(item.tipo) }), _jsx("td", { children: titleLabel(item.tituloSoporteId) }), _jsx("td", { children: _jsx(ValidityStatus, { current: isIdoneidadVigente(item), since: formatVigencia(item) }) }), _jsx("td", { children: _jsxs(RowActions, { children: [_jsx(IconAction, { label: `Editar idoneidad de ${areaNameById(item.areaId)}`, onClick: () => onEdit(item) }), isIdoneidadVigente(item) ? (_jsx(TextLink, { onClick: () => onEnd(item), children: "Finalizar vigencia" })) : null] }) })] }, item.id))) })] }), _jsx("ul", { className: styles.cards, children: items.map((item) => (_jsxs("li", { className: styles.mobileCard, children: [_jsxs("strong", { children: [areaNameById(item.areaId), " \u00B7 ", labelTipoIdoneidad(item.tipo)] }), _jsx("p", { children: subjectLabel(item.asignaturaId) }), _jsx("p", { children: titleLabel(item.tituloSoporteId) }), _jsx(ValidityStatus, { current: isIdoneidadVigente(item), since: formatVigencia(item) }), _jsxs(RowActions, { children: [_jsx(IconAction, { label: `Editar idoneidad de ${areaNameById(item.areaId)}`, onClick: () => onEdit(item) }), isIdoneidadVigente(item) ? (_jsx(TextLink, { onClick: () => onEnd(item), children: "Finalizar vigencia" })) : null] })] }, item.id))) }), items.length === 0 ? (_jsx("p", { className: styles.empty, children: "No hay idoneidades registradas para este docente." })) : null, _jsx("div", { className: styles.note, children: _jsx(Text, { variant: "body", children: "La asignatura es opcional. Sin asignatura, la idoneidad cubre toda el \u00E1rea. El t\u00EDtulo de soporte se toma de los t\u00EDtulos profesionales del docente." }) })] })] }));
}
