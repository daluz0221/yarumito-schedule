import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { PENDING_BACKEND_LABEL, formatTeacherName, labelEstado, toneEstado, } from '../../../content/teachers';
import { Card } from '../../atoms/Card';
import { Text } from '../../atoms/Text';
import { TextLink } from '../../atoms/TextLink';
import { MetaField } from '../../molecules/MetaField';
import { StatusLabel } from '../../molecules/StatusLabel';
import styles from './TeacherSummaryCard.module.css';
export function TeacherSummaryCard({ teacher, documentLabel, areaNombre, }) {
    return (_jsxs(Card, { className: styles.card, children: [_jsxs("div", { className: styles.top, children: [_jsx(Text, { variant: "sectionTitle", children: formatTeacherName(teacher) }), _jsxs("div", { className: styles.topActions, children: [_jsx(StatusLabel, { label: labelEstado(teacher.estado), tone: toneEstado(teacher.estado) }), _jsx(TextLink, { to: `/dashboard/docentes/${teacher.id}/editar`, children: "Editar datos" })] })] }), _jsxs("div", { className: styles.meta, children: [_jsx(MetaField, { label: "Documento", value: documentLabel }), _jsx(MetaField, { label: "\u00C1rea de nombramiento", value: areaNombre || PENDING_BACKEND_LABEL }), _jsx(MetaField, { label: "Estado", value: labelEstado(teacher.estado) })] })] }));
}
